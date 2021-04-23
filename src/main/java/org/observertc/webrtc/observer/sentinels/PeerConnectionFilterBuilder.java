package org.observertc.webrtc.observer.sentinels;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.common.IPAddressConverterProvider;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configs.PeerConnectionFilterConfig;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Prototype
public class PeerConnectionFilterBuilder extends AbstractBuilder implements Function<PeerConnectionFilterConfig, Predicate<PeerConnectionEntity>> {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionFilterBuilder.class);

    @Inject
    Provider<CollectionFilterBuilder> collectionFilterBuilderProvider;

    @Inject
    IPAddressConverterProvider ipAddressConverterProvider;

    @Override
    public Predicate<PeerConnectionEntity> apply(PeerConnectionFilterConfig pcFilterDTO) throws Throwable {
        final List<Predicate<PeerConnectionEntity>> filters = this.makeFilters(pcFilterDTO);
        if (filters.size() < 1) {
            logger.warn("There is no filter condition given for filter {}. Hence it will be always false", pcFilterDTO);
            return pcEntity -> false;
        }
        return pcEntity -> {
            for (Predicate<PeerConnectionEntity> filter : filters) {
                if (!filter.test(pcEntity)) {
                    return false;
                }
            }
            return true;
        };
    }

    public Predicate<PeerConnectionEntity> build() throws Throwable {
        PeerConnectionFilterConfig config = this.convertAndValidate(PeerConnectionFilterConfig.class);
        return this.apply(config);
    }

    private List<Predicate<PeerConnectionEntity>> makeFilters(PeerConnectionFilterConfig filterConfig) {
        List<Predicate<PeerConnectionEntity>> result = new LinkedList<>();
        if (Objects.nonNull(filterConfig.serviceName)) {
            Predicate<PeerConnectionEntity> filter = pcEntity ->
                    Objects.nonNull(pcEntity.peerConnection.serviceName) &&
                            filterConfig.serviceName.equals(pcEntity.peerConnection.serviceName)
                    ;
            result.add(filter);
        }
        if (Objects.nonNull(filterConfig.callName)) {
            Predicate<PeerConnectionEntity> filter = this.makeRegexFilter(filterConfig.callName, pcEntity -> pcEntity.peerConnection.callName);
            result.add(filter);
        }
        if (Objects.nonNull(filterConfig.marker)) {
            Predicate<PeerConnectionEntity> filter = this.makeRegexFilter(filterConfig.marker, pcEntity -> pcEntity.peerConnection.marker);
            result.add(filter);
        }

        if (Objects.nonNull(filterConfig.SSRCs) && !CollectionFilterBuilder.isEmpty(filterConfig.SSRCs)) {
            CollectionFilterBuilder collectionFilterBuilder = collectionFilterBuilderProvider.get();
            Predicate<Collection<Long>> filter = collectionFilterBuilder.build(filterConfig.SSRCs, item -> Long.parseLong(item));
            if (collectionFilterBuilder.isWarned()) {
                logger.warn("While building filter {} a warning is emerged. please check the configuration for the filter", filterConfig.name);
            }
            result.add(pcEntity -> filter.test(pcEntity.SSRCs));
        }

        if (Objects.nonNull(filterConfig.remoteIPs) && !CollectionFilterBuilder.isEmpty(filterConfig.remoteIPs)) {
            CollectionFilterBuilder collectionFilterBuilder = collectionFilterBuilderProvider.get();
            Function<String, String> ipAddressResolver = ipAddressConverterProvider.provideReactiveX();
            Predicate<Collection<String>> filter = collectionFilterBuilder.build(filterConfig.remoteIPs, ipAddressResolver);
            if (collectionFilterBuilder.isWarned()) {
                logger.warn("While building filter {} a warning is emerged. please check the configuration for the filter", filterConfig.name);
            }
            result.add(pcEntity -> filter.test(pcEntity.remoteIPs));
        }
        return result;
    }

    private Predicate<PeerConnectionEntity> makeRegexFilter(String regex, Function<PeerConnectionEntity, String> extractor) {
        Pattern pattern = Pattern.compile(regex);
        return new Predicate<PeerConnectionEntity>() {
            @Override
            public boolean test(PeerConnectionEntity pcEntity) throws Throwable {
                String subject = extractor.apply(pcEntity);
                if (Objects.isNull(subject)) {
                    return false;
                }
                Matcher matcher = pattern.matcher(subject);
                return matcher.find();
            }
        };
    }

}
