package org.observertc.webrtc.observer.entities;

import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.dto.SentinelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SentinelEntity {
    private static final Logger logger = LoggerFactory.getLogger(SentinelEntity.class);
    private final SentinelDTO sentinelDTO;
    private final Predicate<CallEntity> callFilter;
    private final Predicate<PeerConnectionEntity> pcFilter;

    public SentinelEntity(SentinelDTO sentinelDTO, Predicate<CallEntity> callFilter, Predicate<PeerConnectionEntity> pcFilter) {
        this.sentinelDTO = sentinelDTO;
        this.callFilter = callFilter;
        this.pcFilter = pcFilter;
    }

    public static SentinelEntity.Builder builder() {
        return new SentinelEntity.Builder();
    }

    public String getName() {return this.sentinelDTO.name;}

    public boolean isExposed() { return this.sentinelDTO.expose; }

    public boolean isReported() { return this.sentinelDTO.report; }

    public boolean testCall(CallEntity callEntity) throws Throwable {
        boolean result = this.callFilter.test(callEntity);
        return result;
    }

    public boolean testPeerConnection(PeerConnectionEntity pcEntity) throws Throwable {
        boolean result = this.pcFilter.test(pcEntity);
        return result;
    }

    public static class Builder {
        public SentinelDTO sentinelDTO;
        public Predicate<CallEntity> callFilter;
        public Predicate<PeerConnectionEntity> pcFilter;


        public SentinelEntity build() {
            Objects.requireNonNull(this.sentinelDTO);

            if (Objects.isNull(this.callFilter) && Objects.isNull(this.pcFilter)) {
                logger.warn("No filter was defined for sentinel {}. It will always be false", sentinelDTO.name);
                this.callFilter = callEntity -> false;
                this.pcFilter = pcEntity -> false;
            } else if (Objects.isNull(this.callFilter)) {
                this.callFilter = callEntity -> false;
            } else if (Objects.isNull(this.pcFilter)) {
                this.pcFilter = pcEntity -> false;
            }

            return new SentinelEntity(this.sentinelDTO, this.callFilter, this.pcFilter);
        }

        public Builder withCallFilter(Predicate<CallEntity> filter) {
            this.callFilter = filter;
            return this;
        }

        public Builder withPCFilter(Predicate<PeerConnectionEntity> filter) {
            this.pcFilter = filter;
            return this;
        }

        public Builder withSentinelDTO(SentinelDTO sentinelDTO) {
            this.sentinelDTO = sentinelDTO;
            return this;
        }
    }
}
