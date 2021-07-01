package org.observertc.webrtc.observer.repositories.tasks;

import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.Task;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Singleton
public class CreateCallEventReportsTaskProvider {

    @Inject
    Provider<CreateCallEventReportsTask> createCallEventReportsProvider;

    public CallEventReportTask<CallDTO> getCreateCallStartedReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.CALL_STARTED).withCallEventMessage("Call is started"),
                task::withCallDTO,
                task::withCallDTOAndTimestamp
        );
    }

    public CallEventReportTask<CallDTO> getCreateCallEndedReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.CALL_ENDED).withCallEventMessage("Call is ended"),
                task::withCallDTO,
                task::withCallDTOAndTimestamp
        );
    }

    public CallEventReportTask<ClientDTO> getCreateClientJoinedReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.CLIENT_JOINED).withCallEventMessage("Client is joined to a call"),
                task::withClientDTO,
                task::withClientDTOAndTimestamp
        );
    }

    public CallEventReportTask<ClientDTO> getCreateClientLeftReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.CLIENT_LEFT).withCallEventMessage("Client is left the call"),
                task::withClientDTO,
                task::withClientDTOAndTimestamp
        );
    }

    public CallEventReportTask<PeerConnectionDTO> getCreatePeerConnectionOpenedReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.PEER_CONNECTION_OPENED).withCallEventMessage("Peer connection is created by a client"),
                task::withPeerConnectionDTO,
                task::withPeerConnectionDTOAndTimestamp
        );
    }

    public CallEventReportTask<PeerConnectionDTO> getCreatePeerConnectionClosedReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.PEER_CONNECTION_CLOSED).withCallEventMessage("Peer connection is closed by a client"),
                task::withPeerConnectionDTO,
                task::withPeerConnectionDTOAndTimestamp
        );
    }

    public CallEventReportTask<MediaTrackDTO> getCreateMediaTrackAddedReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.MEDIA_TRACK_ADDED).withCallEventMessage("Media Track is added to the peer connection"),
                task::withMediaTrackDTO,
                task::withMediaTrackDTOAndTimestamp
        );
    }

    public CallEventReportTask<MediaTrackDTO> getCreateMediaTrackRemovedReportsTask() {
        var task = createCallEventReportsProvider.get();
        return makeCallEventReportTask(
                task.withCallEventType(CallEventType.MEDIA_TRACK_REMOVED).withCallEventMessage("Media Track is removed from the peer connection"),
                task::withMediaTrackDTO,
                task::withMediaTrackDTOAndTimestamp
        );
    }

    private<T> CallEventReportTask<T> makeCallEventReportTask(CreateCallEventReportsTask task,
                                                              Consumer<T> acceptDTO,
                                                              BiConsumer<T, Long> acceptDTOAndTimestamp
    ) {
        return new CallEventReportTask<T>() {
            @Override
            public CallEventReportTask<T> withDTO(T DTO) {
                acceptDTO.accept(DTO);
                return this;
            }

            @Override
            public CallEventReportTask<T> withDTOAndTimestamp(T DTO, Long timestamp) {
                return null;
            }

            @Override
            public Task<List<CallEventReport>> execute() {
                task.execute();
                return this;
            }

            @Override
            public boolean succeeded() {
                return task.succeeded();
            }

            @Override
            public List<CallEventReport> getResult() {
                return task.getResult();
            }

            @Override
            public List<CallEventReport> getResultOrDefault(List<CallEventReport> defaultValue) {
                return task.getResultOrDefault(defaultValue);
            }
        };
    }
}
