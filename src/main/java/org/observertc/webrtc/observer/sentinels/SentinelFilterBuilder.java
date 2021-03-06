package org.observertc.webrtc.observer.sentinels;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.dto.SentinelFilterDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Prototype
public class SentinelFilterBuilder extends AbstractBuilder implements Function<SentinelFilterDTO, Predicate<CallEntity>> {
    private static final Logger logger = LoggerFactory.getLogger(SentinelFilterBuilder.class);

    @Inject
    Provider<CollectionFilterBuilder> collectionFilterBuilderProvider;

    @Override
    public Predicate<CallEntity> apply(SentinelFilterDTO sentinelFilterDTO) throws Throwable {
        final List<Predicate<CallEntity>> filters = this.makeFilters(sentinelFilterDTO);
        if (filters.size() < 1) {
            logger.warn("There is no filter condition gven for filter {}. Hence it will be always false", sentinelFilterDTO);
            return callEntity -> false;
        }
        return callEntity -> {
            for (Predicate<CallEntity> filter : filters) {
                if (!filter.test(callEntity)) {
                    return false;
                }
            }
            return true;
        };
    }

    public Predicate<CallEntity> build() throws Throwable {
        SentinelFilterDTO config = this.convertAndValidate(SentinelFilterDTO.class);
        return this.apply(config);
    }

    private List<Predicate<CallEntity>> makeFilters(SentinelFilterDTO filterConfig) {
        List<Predicate<CallEntity>> result = new LinkedList<>();
        if (Objects.nonNull(filterConfig.serviceName)) {
            Predicate<CallEntity> filter = callEntity ->
                    Objects.nonNull(callEntity.call.serviceName) &&
                            filterConfig.serviceName.equals(callEntity.call.serviceName)
                    ;
            result.add(filter);
        }
        if (Objects.nonNull(filterConfig.callName)) {
            Predicate<CallEntity> filter = this.makeRegexFilter(filterConfig.callName, callEntity -> callEntity.call.callName);
            result.add(filter);
        }
        if (Objects.nonNull(filterConfig.marker)) {
            Predicate<CallEntity> filter = this.makeRegexFilter(filterConfig.marker, callEntity -> callEntity.call.marker);
            result.add(filter);
        }
        if (Objects.nonNull(filterConfig.browserIds) && !CollectionFilterBuilder.isEmpty(filterConfig.browserIds)) {
            CollectionFilterBuilder collectionFilterBuilder = collectionFilterBuilderProvider.get();
            Predicate<Collection<String>> filter = collectionFilterBuilder.build(filterConfig.browserIds, item -> item);
            if (collectionFilterBuilder.isWarned()) {
                logger.warn("While building filter {} a warning is emerged. please check the configuration for the filter", filterConfig.name);
            }
            result.add(callEntity -> {
                Set<String> browserIds = callEntity.peerConnections.values().stream()
                        .map(pc -> pc.peerConnection.browserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                return filter.test(browserIds);
            });
        }
        if (Objects.nonNull(filterConfig.peerConnections) && !CollectionFilterBuilder.isEmpty(filterConfig.peerConnections)) {
            CollectionFilterBuilder collectionFilterBuilder = collectionFilterBuilderProvider.get();
            Predicate<Collection<UUID>> filter = collectionFilterBuilder.build(filterConfig.peerConnections, UUID::fromString);
            if (collectionFilterBuilder.isWarned()) {
                logger.warn("While building filter {} a warning is emerged. please check the configuration for the filter", filterConfig.name);
            }
            result.add(callEntity -> {
                Set<UUID> pcUUIDs = callEntity.peerConnections.values().stream()
                        .map(pc -> pc.peerConnection.peerConnectionUUID)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                return filter.test(pcUUIDs);
            });
        }
        if (Objects.nonNull(filterConfig.SSRCs) && !CollectionFilterBuilder.isEmpty(filterConfig.SSRCs)) {
            CollectionFilterBuilder collectionFilterBuilder = collectionFilterBuilderProvider.get();
            Predicate<Collection<Long>> filter = collectionFilterBuilder.build(filterConfig.SSRCs, item -> Long.parseLong(item));
            if (collectionFilterBuilder.isWarned()) {
                logger.warn("While building filter {} a warning is emerged. please check the configuration for the filter", filterConfig.name);
            }
            result.add(callEntity -> filter.test(callEntity.SSRCs));
        }
        return result;
    }

    private Predicate<CallEntity> makeRegexFilter(String regex, Function<CallEntity, String> extractor) {
        Pattern pattern = Pattern.compile(regex);
        return new Predicate<CallEntity>() {
            @Override
            public boolean test(CallEntity callEntity) throws Throwable {
                String subject = extractor.apply(callEntity);
                if (Objects.isNull(subject)) {
                    return false;
                }
                Matcher matcher = pattern.matcher(subject);
                return matcher.find();
            }
        };
    }

}
