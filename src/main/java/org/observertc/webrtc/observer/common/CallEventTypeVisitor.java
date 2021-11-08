package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.functions.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public interface CallEventTypeVisitor<TIn, TOut> extends BiFunction<TIn, CallEventType, TOut> {
    Logger logger = LoggerFactory.getLogger(CallEventTypeVisitor.class);

    static <RIn, ROut>CallEventTypeVisitor<RIn, ROut> createFunctionalVisitor(
            Function<RIn, ROut> callStartedProcess,
            Function<RIn, ROut> callEndedProcess,
            Function<RIn, ROut> clientJoinedProcess,
            Function<RIn, ROut> clientLeftProcess,
            Function<RIn, ROut> peerConnectionOpenedProcess,
            Function<RIn, ROut> peerConnectionClosedProcess,
            Function<RIn, ROut> mediaTrackAddedProcess,
            Function<RIn, ROut> mediaTrackRemovedProcess
    ) {
        return new CallEventTypeVisitor<RIn, ROut>() {
            @Override
            public ROut visitCallStarted(RIn obj) {
                return callStartedProcess.apply(obj);
            }

            @Override
            public ROut visitCallEnded(RIn obj) {
                return callEndedProcess.apply(obj);
            }

            @Override
            public ROut visitClientJoined(RIn obj) {
                return clientJoinedProcess.apply(obj);
            }

            @Override
            public ROut visitClientLeft(RIn obj) {
                return clientLeftProcess.apply(obj);
            }

            @Override
            public ROut visitPeerConnectionOpened(RIn obj) {
                return peerConnectionOpenedProcess.apply(obj);
            }

            @Override
            public ROut visitPeerConnectionClosed(RIn obj) {
                return peerConnectionClosedProcess.apply(obj);
            }

            @Override
            public ROut visitMediaTrackAdded(RIn obj) {
                return mediaTrackAddedProcess.apply(obj);
            }

            @Override
            public ROut visitMediaTrackRemoved(RIn obj) {
                return mediaTrackRemovedProcess.apply(obj);
            }
        };
    }

    static <RIn>CallEventTypeVisitor<RIn, Void> createConsumerVisitor (
            Consumer<RIn> callStartedProcess,
            Consumer<RIn> callEndedProcess,
            Consumer<RIn> clientJoinedProcess,
            Consumer<RIn> clientLeftProcess,
            Consumer<RIn> peerConnectionOpenedProcess,
            Consumer<RIn> peerConnectionClosedProcess,
            Consumer<RIn> mediaTrackAddedProcess,
            Consumer<RIn> mediaTrackRemovedProcess
    ) {
        return new CallEventTypeVisitor<RIn, Void>() {
            @Override
            public Void visitCallStarted(RIn obj) {
                callStartedProcess.accept(obj);
                return null;
            }

            @Override
            public Void visitCallEnded(RIn obj) {
                callEndedProcess.accept(obj);
                return null;
            }

            @Override
            public Void visitClientJoined(RIn obj) {
                clientJoinedProcess.accept(obj);
                return null;
            }

            @Override
            public Void visitClientLeft(RIn obj) {
                clientLeftProcess.accept(obj);
                return null;
            }

            @Override
            public Void visitPeerConnectionOpened(RIn obj) {
                peerConnectionOpenedProcess.accept(obj);
                return null;
            }

            @Override
            public Void visitPeerConnectionClosed(RIn obj) {
                peerConnectionClosedProcess.accept(obj);
                return null;
            }

            @Override
            public Void visitMediaTrackAdded(RIn obj) {
                mediaTrackAddedProcess.accept(obj);
                return null;
            }

            @Override
            public Void visitMediaTrackRemoved(RIn obj) {
                mediaTrackRemovedProcess.accept(obj);
                return null;
            }
        };
    }

    static CallEventTypeVisitor<Void, Void> createActionVisitor (
            Runnable callStartedProcess,
            Runnable callEndedProcess,
            Runnable clientJoinedProcess,
            Runnable clientLeftProcess,
            Runnable peerConnectionOpenedProcess,
            Runnable peerConnectionClosedProcess,
            Runnable mediaTrackAddedProcess,
            Runnable mediaTrackRemovedProcess
    ) {
        return new CallEventTypeVisitor<Void, Void>() {
            @Override
            public Void visitCallStarted(Void obj) {
                callStartedProcess.run();
                return null;
            }

            @Override
            public Void visitCallEnded(Void obj) {
                callEndedProcess.run();
                return null;
            }

            @Override
            public Void visitClientJoined(Void obj) {
                clientJoinedProcess.run();
                return null;
            }

            @Override
            public Void visitClientLeft(Void obj) {
                clientLeftProcess.run();
                return null;
            }

            @Override
            public Void visitPeerConnectionOpened(Void obj) {
                peerConnectionOpenedProcess.run();
                return null;
            }

            @Override
            public Void visitPeerConnectionClosed(Void obj) {
                peerConnectionClosedProcess.run();
                return null;
            }

            @Override
            public Void visitMediaTrackAdded(Void obj) {
                mediaTrackAddedProcess.run();
                return null;
            }

            @Override
            public Void visitMediaTrackRemoved(Void obj) {
                mediaTrackRemovedProcess.run();
                return null;
            }
        };
    }

    @Override
    default TOut apply(TIn obj, CallEventType callEventType) throws Throwable {
        switch (callEventType) {
            case CALL_STARTED:
                return this.visitCallStarted(obj);
            case CALL_ENDED:
                return this.visitCallEnded(obj);
            case CLIENT_JOINED:
                return this.visitClientJoined(obj);
            case CLIENT_LEFT:
                return this.visitClientLeft(obj);
            case PEER_CONNECTION_OPENED:
                return this.visitPeerConnectionOpened(obj);
            case PEER_CONNECTION_CLOSED:
                return this.visitPeerConnectionClosed(obj);
            case MEDIA_TRACK_ADDED:
                return this.visitMediaTrackAdded(obj);
            case MEDIA_TRACK_REMOVED:
                return this.visitMediaTrackRemoved(obj);
            default:
                logger.warn("Unknown event type {}", callEventType);
                return null;
        }
    }


    TOut visitCallStarted(TIn obj);
    TOut visitCallEnded(TIn obj);

    TOut visitClientJoined(TIn obj);
    TOut visitClientLeft(TIn obj);

    TOut visitPeerConnectionOpened(TIn obj);
    TOut visitPeerConnectionClosed(TIn obj);

    TOut visitMediaTrackAdded(TIn obj);
    TOut visitMediaTrackRemoved(TIn obj);


}
