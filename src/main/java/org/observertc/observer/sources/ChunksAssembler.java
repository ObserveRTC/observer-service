package org.observertc.observer.sources;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.common.TimeLimitedMap;

import java.time.Duration;
import java.util.Objects;

@Prototype
public class ChunksAssembler implements Consumer<ReceivedMessage> {
    private Subject<ReceivedMessage> assembledMessages = PublishSubject.create();

    private final TimeLimitedMap<String, Chunks> chunks;

    public ChunksAssembler() {
        this.chunks = new TimeLimitedMap<>(Duration.ofSeconds(30));

    }

    @Override
    public void accept(ReceivedMessage receivedMessage) throws Throwable {
        var sessionId = receivedMessage.sessionId;
        Chunks chunks = this.chunks.get(sessionId);
        if (Objects.isNull(chunks)) {
            chunks = new Chunks(sessionId);
            this.chunks.put(sessionId, chunks);
        }
        chunks.add(receivedMessage);
        if (chunks.isReady()) {
            this.forward(sessionId);
        }
    }

    private void forward(String sessionId) {
        var chunks = this.chunks.remove(sessionId);
        if (Objects.isNull(chunks)) {
            return;
        }
        var assembledMessage = chunks.assemble();
        if (Objects.isNull(assembledMessage)) {
            return;
        }
        this.assembledMessages.onNext(assembledMessage);
    }
}
