package org.observertc.webrtc.observer.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.common.UUIDAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class StoredRequests {

    @Inject
    HazelcastMaps hazelcastMaps;
    private ObjectMapper mapper = new ObjectMapper();

    private String getCompleteRtpStreamSfuRtpPadsRequestId(UUID rtpStreamId) {
        return String.format("COMPLETE_SFU_RTP_PAD_BY_RTP_STREAM_REQUEST-%s", UUIDAdapter.toStringOrDefault(rtpStreamId, "NO_EXISTING"));
    }

    public void addCompleteRtpStreamSfuRtpPadRequests(UUID... rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds)) {
            return;
        }
        this.addCompleteRtpStreamSfuRtpPadRequests(Arrays.asList(rtpStreamIds));
    }

    public void addCompleteRtpStreamSfuRtpPadRequests(Collection<UUID> rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds) || rtpStreamIds.size() < 1) {
            return;
        }
        Map<String, byte[]> requests = rtpStreamIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        rtpStreamId -> getCompleteRtpStreamSfuRtpPadsRequestId(rtpStreamId),
                        rtpStreamId -> UUIDAdapter.toBytes(rtpStreamId)
                ));
        this.hazelcastMaps.getRequests().putAll(requests);
    }

    public Set<UUID> removeCompleteRtpStreamSfuRtpPadsRequests(UUID... rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds)) {
            return Collections.EMPTY_SET;
        }
        List<UUID> list = Arrays.asList(rtpStreamIds);
        return this.removeCompleteRtpStreamSfuRtpPadsRequests(list);
    }

    public Set<UUID> removeCompleteRtpStreamSfuRtpPadsRequests(Collection<UUID> rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds) || rtpStreamIds.size() < 1) {
            return Collections.EMPTY_SET;
        }
        Set<String> requestIds = rtpStreamIds.stream()
                .filter(Objects::nonNull)
                .map(this::getCompleteRtpStreamSfuRtpPadsRequestId)
                .collect(Collectors.toSet());
        Map<String, byte[]> requests = this.hazelcastMaps.getRequests().getAll(requestIds);
        if (requests.size() < 1) {
            return Collections.EMPTY_SET;
        }
        Set<UUID> result = requests.values()
                .stream()
                .filter(Objects::nonNull)
                .map(UUIDAdapter::toUUID)
                .collect(Collectors.toSet());
        return result;
    }

}
