package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.ServiceRoomId;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FindCallIdsByRtpStreamIdsTask extends ChainedTask<Map<UUID, UUID>> {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchCallsTask fetchCallsTask;

    private Set<UUID> rtpStreamIds = new HashSet<>();


    @PostConstruct
    void setup() {

        new Builder<>(this)
            .<Set<ServiceRoomId>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> { // input was invoked, so we may got some names through that
                    if (Objects.isNull(input)) {
                        return;
                    }
            })
            .addTerminalSupplier("Find Call Ids for Stream Ids", () -> {
                Map<UUID, UUID> result = new HashMap<>();
                if (this.rtpStreamIds.size() < 1) {
                    return result;
                }
                Map<UUID, UUID> foundMediaTrackIds = this.hazelcastMaps.getRtpStreamIdsToOutboundTrackIds().getAll(this.rtpStreamIds);
                if (foundMediaTrackIds.size() < 1) {
                    return result;
                }
                Set<UUID> trackIds = new HashSet<>(foundMediaTrackIds.values());
                Map<UUID, MediaTrackDTO> outboundMediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(trackIds);
                if (outboundMediaTrackDTOs.size() < 1) {
                    return result;
                }
                outboundMediaTrackDTOs.values().forEach(mediaTrackDTO -> {
                    result.put(mediaTrackDTO.rtpStreamId, mediaTrackDTO.callId);
                });
                return result;
            })
        .build();
    }

    public FindCallIdsByRtpStreamIdsTask withRtpStreamId(UUID rtpStreamId) {
        Objects.requireNonNull(rtpStreamId);
        this.rtpStreamIds.add(rtpStreamId);
        return this;
    }

    public FindCallIdsByRtpStreamIdsTask withRtpStreamIds(Set<UUID> rtpStreamIds) {
        Objects.requireNonNull(rtpStreamIds);
        this.rtpStreamIds.addAll(rtpStreamIds);
        return this;
    }

    @Override
    protected void validate() {

    }
}
