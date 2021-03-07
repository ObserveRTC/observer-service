package org.observertc.webrtc.observer.entities;

import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.dto.SentinelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SentinelEntity implements Predicate<CallEntity>{
    private static final Logger logger = LoggerFactory.getLogger(SentinelEntity.class);
    private final SentinelDTO sentinelDTO;
    private final Predicate<CallEntity> filter;

    public SentinelEntity(SentinelDTO sentinelDTO, Predicate<CallEntity> filter) {
        this.sentinelDTO = sentinelDTO;
        this.filter = filter;
    }

    public static SentinelEntity.Builder builder() {
        return new SentinelEntity.Builder();
    }

    public String getName() {return this.sentinelDTO.name;}

    public boolean isExposed() { return this.sentinelDTO.expose; }

    public boolean isReported() { return this.sentinelDTO.report; }

    public boolean streamMetrics() {
        return this.sentinelDTO.streamMetrics;
    }

    @Override
    public boolean test(CallEntity callEntity) throws Throwable {
        boolean result = this.filter.test(callEntity);
        return result;
    }

    public static class Builder {
        public SentinelDTO sentinelDTO;
        public Predicate<CallEntity> filter;


        public SentinelEntity build() {
            Objects.requireNonNull(this.sentinelDTO);
            if (Objects.isNull(this.filter)) {
                logger.warn("No filter was defined for sentinel {}. It will always be false", sentinelDTO.name);
                this.filter = callEntity -> false;
            }
            return new SentinelEntity(this.sentinelDTO, this.filter);
        }

        public Builder withFilter(Predicate<CallEntity> filter) {
            this.filter = filter;
            return this;
        }

        public Builder withSentinelDTO(SentinelDTO sentinelDTO) {
            this.sentinelDTO = sentinelDTO;
            return this;
        }
    }
}
