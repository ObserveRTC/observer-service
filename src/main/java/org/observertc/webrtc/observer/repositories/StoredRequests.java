package org.observertc.webrtc.observer.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.configs.ObserverConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class StoredRequests {

    public static final String COMPLETE_SFU_RTP_PAD_BY_RTP_STREAM_REQUEST = "COMPLETE_SFU_RTP_PAD_BY_RTP_STREAM_REQUEST";

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ObserverConfig observerConfig;

    private ObjectMapper mapper = new ObjectMapper();

    private String getCompleteRtpStreamSfuRtpPadsRequestId(UUID rtpStreamId) {
        return String.format("%s-%s", COMPLETE_SFU_RTP_PAD_BY_RTP_STREAM_REQUEST, UUIDAdapter.toStringOrDefault(rtpStreamId, "NO_EXISTING"));
    }

    public boolean isCompleteRtpStreamSfuRtpPadRequest(String key) {
        if (Objects.isNull(key)) {
            return false;
        }
        return key.startsWith(COMPLETE_SFU_RTP_PAD_BY_RTP_STREAM_REQUEST);
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
        int reportSfuRtpPadWithCallIdTimeoutInS = this.observerConfig.evaluators.reportSfuRtpPadWithCallIdTimeoutInS;
        requests.entrySet().stream().forEach(entry -> {
            this.hazelcastMaps.getRequests().put(entry.getKey(),
                    entry.getValue(),
                    reportSfuRtpPadWithCallIdTimeoutInS,
                    TimeUnit.SECONDS
            );
        });
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
