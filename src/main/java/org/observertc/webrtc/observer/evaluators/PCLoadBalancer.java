package org.observertc.webrtc.observer.evaluators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.hazelcast.map.IMap;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.common.Sleeper;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class PCLoadBalancer implements ObservableOperator<ObservedPCS, ObservedPCS> {
    private static final Logger logger = LoggerFactory.getLogger(PCLoadBalancer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static String getQueueName(UUID instanceUUID) {
        return String.format("%s-%s", PCLoadBalancer.class.getSimpleName(), instanceUUID.toString());
    }


    @Inject
    ObserverHazelcast observerHazelcast;

    private IMap<UUID, UUID> pcKeysToInstances;
//    private IQueue<byte[]> queue;
    private UUID localEndpointUUID;
    private Subject<ObservedPCS> incomingMessages = PublishSubject.create();
    private MessageReceiver messageReceiver;


    @PostConstruct
    void setup() {
        this.pcKeysToInstances = this.observerHazelcast.getInstance().getMap(PCLoadBalancer.class.getSimpleName());
        this.localEndpointUUID = this.observerHazelcast.getInstance().getLocalEndpoint().getUuid();
        this.messageReceiver = new MessageReceiver();
//        new Thread(new MessageConsumer()).start();

        //        String queueName = getQueueName(localEndpointUUID);
//        IQueue<byte[]> queue = this.observerHazelcast.getInstance().getQueue(queueName);
//
//        Observable.fromSupplier(new Supplier<>)
//                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
//                .map(this::decode)
//                .filter(Objects::nonNull)
//                ;
    }

    @PreDestroy
    void teardown() {
        this.messageReceiver.run = false;
        if (observerHazelcast.getInstance().getLifecycleService().isRunning()) {
            Set<UUID> keys = this.pcKeysToInstances.localKeySet();
            keys.forEach(this.pcKeysToInstances::delete);
        }

    }

    private byte[] encode(ObservedPCS observedPCS) {
        try {
            byte[] message = OBJECT_MAPPER.writeValueAsBytes(observedPCS);
            return message;
        } catch (Throwable t) {
            logger.warn("Unexpected error occurred in encoding", t);
            return null;
        }
    }


    @Override
    public @NonNull Observer<? super ObservedPCS> apply(@NonNull Observer<? super ObservedPCS> observer) throws Throwable {
        new Thread(this.messageReceiver).start();
        Subject<ObservedPCS> relayedMessages = PublishSubject.create();
        var result = new MessageRouter(relayedMessages);
        Observable.merge(relayedMessages, this.incomingMessages)
                .subscribe(observer);
        return result;
    }


    private class MessageRouter implements Observer<ObservedPCS> {
        final Map<UUID, IQueue<byte[]>> queues = new HashMap<>();
        final Subject<ObservedPCS> output;
        private Disposable disposable;

        public MessageRouter(Subject<ObservedPCS> output) {
            this.output = output;
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            this.disposable = d;
        }

        @Override
        public void onNext(@NonNull ObservedPCS observedPCS) {
            UUID endpointUUID = pcKeysToInstances.putIfAbsent(observedPCS.peerConnectionUUID, localEndpointUUID, 60, TimeUnit.SECONDS);
            if (endpointUUID.equals(observedPCS.peerConnectionUUID)) {
                output.onNext(observedPCS);
                return;
            }
            byte[] message = encode(observedPCS);
            if (Objects.isNull(message)) {
                return;
            }
            try {
                IQueue<byte[]> queue = this.queues.get(endpointUUID);
                if (Objects.isNull(queue)) {
                    String queueName = getQueueName(endpointUUID);
                    queue = observerHazelcast.getInstance().getQueue(queueName);
                    this.queues.put(endpointUUID, queue);
                }
                queue.offer(message);
            } catch (Throwable t) {
                logger.warn("Unexpected error occurred", t);
            }

        }

        @Override
        public void onError(@NonNull Throwable e) {
            if (Objects.nonNull(this.disposable)) {
                if (!this.disposable.isDisposed()) {
                    this.disposable.dispose();
                }
            }
            output.onError(e);
        }

        @Override
        public void onComplete() {
            output.onComplete();
        }
    }

    private class MessageReceiver implements Runnable {

        volatile boolean run = true;
        final IQueue<byte[]> queue;

        MessageReceiver() {
//            PCLoadBalancer parent = PCLoadBalancer.this;
            String queueName = getQueueName(localEndpointUUID);
            this.queue = observerHazelcast.getInstance().getQueue(queueName);
        }

        @Override
        public void run() {
            Sleeper coercedWaiting = new Sleeper(() -> 1000);
            while (this.run) {

                byte[] bytes = this.poll();
                if (Objects.isNull(bytes)) {
                    if (observerHazelcast.getInstance().getLifecycleService().isRunning()) {
                        this.run = false;
                    }
                    continue;
                }

                ObservedPCS observedPCS = this.decode(bytes);
                if (Objects.isNull(observedPCS)) {
                    if (observerHazelcast.getInstance().getLifecycleService().isRunning()) {
                        this.run = false;
                    }
                    coercedWaiting.run();
                    continue;
                }

                this.send(observedPCS);
            }
        }

        private int consecutivePollingErrors = 0;
        private byte[] poll() {
            try {
                byte[] result = queue.poll(1000, TimeUnit.MILLISECONDS);
                this.consecutivePollingErrors = 0;
                return result;
            } catch(Throwable t) {
                logger.warn("Unexpected error occurred while polling queue", t);
                if (2 < ++this.consecutivePollingErrors) {
                    logger.error("{} is ordered to be stopped due to 3 consecutive errors", this.getClass().getSimpleName());
                    this.run = false;
                }
                return null;
            }
        }

        private int consecutiveDecodingErrors = 0;
        private ObservedPCS decode(byte[] message) {
            try {
                ObservedPCS result = OBJECT_MAPPER.readValue(message, ObservedPCS.class);
                this.consecutiveDecodingErrors = 0;
                return result;
            } catch (Throwable t) {
                logger.warn("Unexpected error occurred in decoding", t);
                if (2 < ++this.consecutiveDecodingErrors) {
                    logger.error("{} is ordered to be stopped due to 3 consecutive errors", this.getClass().getSimpleName());
                    this.run = false;
                }
                return null;
            }
        }

        private int consecutiveSendingErrors = 0;
        private void send(ObservedPCS observedPCS) {
            try {
                incomingMessages.onNext(observedPCS);
                this.consecutiveSendingErrors = 0;
            } catch (Throwable t) {
                logger.warn("Unexpected error occurred in sending", t);
                if (2 < ++this.consecutiveSendingErrors) {
                    logger.error("{} is ordered to be stopped due to 3 consecutive errors", this.getClass().getSimpleName());
                    this.run = false;
                }
            }
        }


    }
}
