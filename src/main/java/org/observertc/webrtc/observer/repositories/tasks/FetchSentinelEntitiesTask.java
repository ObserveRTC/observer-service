package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.configs.CallFilterConfig;
import org.observertc.webrtc.observer.configs.PeerConnectionFilterConfig;
import org.observertc.webrtc.observer.configs.SentinelConfig;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.configs.stores.CallFiltersStore;
import org.observertc.webrtc.observer.configs.stores.PeerConnectionFiltersStore;
import org.observertc.webrtc.observer.configs.stores.SentinelsStore;
import org.observertc.webrtc.observer.sentinels.CallFilterBuilder;
import org.observertc.webrtc.observer.sentinels.PeerConnectionFilterBuilder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Prototype
public class FetchSentinelEntitiesTask extends ChainedTask<Map<String, SentinelEntity>> {

    private Set<String> sentinelNames = new HashSet<>();
    private Map<String, SentinelConfig> sentinelDTOs = null;
    private Map<String, CallFilterConfig> callFilterDTOs = new HashMap<>();
    private Map<String, PeerConnectionFilterConfig> pcFilterDTOs = new HashMap<>();

    @Inject
    CallFilterBuilder callFilterBuilder;

    @Inject
    PeerConnectionFilterBuilder peerConnectionFilterBuilder;

    @Inject
    SentinelsStore sentinelsStore;

    @Inject
    CallFiltersStore callFiltersStore;

    @Inject
    PeerConnectionFiltersStore pcFiltersStore;

    @PostConstruct
    void setup() {
        new Builder<Map<String, SentinelEntity>>(this)
            .addActionStage("Fetch SentinelDTOs",
                () -> {
                    this.sentinelDTOs = sentinelsStore.findByNames(this.sentinelNames);
                })
            .addBreakCondition(resultHolder -> {
                if (Objects.isNull(this.sentinelDTOs) || this.sentinelDTOs.size() < 1) {
                    resultHolder.set(Collections.EMPTY_MAP);
                    return true;
                }
                return false;
            })
            .addActionStage("Fetch Filters",
                () -> {
                    Set<String> filterNames = new HashSet<>();
                    this.sentinelDTOs.values().stream().filter(s -> Objects.nonNull(s.callFilters.anyMatch)).flatMap(s -> Arrays.stream(s.callFilters.anyMatch)).forEach(filterNames::add);
                    this.sentinelDTOs.values().stream().filter(s -> Objects.nonNull(s.callFilters.allMatch)).flatMap(s -> Arrays.stream(s.callFilters.allMatch)).forEach(filterNames::add);
                    this.callFilterDTOs = this.callFiltersStore.findByNames(filterNames);
                    filterNames.clear();
                    this.sentinelDTOs.values().stream().filter(s -> Objects.nonNull(s.pcFilters.anyMatch)).flatMap(s -> Arrays.stream(s.pcFilters.anyMatch)).forEach(filterNames::add);
                    this.sentinelDTOs.values().stream().filter(s -> Objects.nonNull(s.pcFilters.allMatch)).flatMap(s -> Arrays.stream(s.pcFilters.allMatch)).forEach(filterNames::add);
                    this.pcFilterDTOs = this.pcFiltersStore.findByNames(filterNames);
                    filterNames.clear();
                })
            .addBreakCondition(resultHolder -> {
                if (Objects.isNull(this.callFilterDTOs) || this.callFilterDTOs.size() < 1) {
                    if (Objects.isNull(this.pcFilterDTOs) || this.pcFilterDTOs.size() < 1) {
                        getLogger().info("No Filter has been found for sentinels {}", this.sentinelDTOs);
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                }
                return false;
            })
            .addTerminalSupplier("Build Sentinel Entities", () -> {
                Map<String, SentinelEntity> result = new HashMap<>();
                for (SentinelConfig sentinelConfig : this.sentinelDTOs.values()) {
                    SentinelEntity sentinelEntity = this.makeSentinelEntity(sentinelConfig);
                    if (Objects.isNull(sentinelEntity)) {
                        continue;
                    }
                    result.put(sentinelEntity.getName(), sentinelEntity);
                }
                return Collections.unmodifiableMap(result);
            })
        .build();
    }

    private SentinelEntity makeSentinelEntity(SentinelConfig sentinelConfig) {
        if (Objects.isNull(sentinelConfig)) {
            return null;
        }
        if (Objects.isNull(sentinelConfig.callFilters) ) {
            getLogger().warn("Cannot instantiate sentinel {}, because there call filter or pcfilter is null", sentinelConfig);
            return null;
        } else if (
                Objects.isNull(sentinelConfig.callFilters.allMatch) ||
                Objects.isNull(sentinelConfig.callFilters.anyMatch) ||
                Objects.isNull(sentinelConfig.pcFilters.allMatch) ||
                Objects.isNull(sentinelConfig.pcFilters.anyMatch)
        ) {
            getLogger().warn("Cannot instantiate sentinel {}, because one of the internal filter is null pcfilter or callfilter", sentinelConfig);
            return null;
        }

        if (sentinelConfig.callFilters.allMatch.length < 1 && sentinelConfig.callFilters.anyMatch.length < 1 &&
            sentinelConfig.pcFilters.allMatch.length < 1 && sentinelConfig.pcFilters.anyMatch.length < 1
        ) {
            getLogger().warn("Sentinel {} does not have any filter configured, it will not be built", sentinelConfig);
            return null;
        }

        List<Predicate<CallEntity>> allCallsMatch = Arrays.stream(sentinelConfig.callFilters.allMatch).map(this::makeCallFilter).collect(Collectors.toList());
        List<Predicate<CallEntity>> anyCallsMatch = Arrays.stream(sentinelConfig.callFilters.anyMatch).map(this::makeCallFilter).collect(Collectors.toList());
        final Function<CallEntity, Boolean> callFilterFunc = this.makePredicate(allCallsMatch, anyCallsMatch);

        Predicate<CallEntity> callFilter;

        if (Objects.nonNull(callFilterFunc)) {
            callFilter = callFilterFunc::apply;
        } else {
            callFilter = c -> false;
        }

        List<Predicate<PeerConnectionEntity>> allPCsMatch = Arrays.stream(sentinelConfig.pcFilters.allMatch).map(this::makePCFilter).collect(Collectors.toList());
        List<Predicate<PeerConnectionEntity>> anyPCsMatch = Arrays.stream(sentinelConfig.pcFilters.anyMatch).map(this::makePCFilter).collect(Collectors.toList());
        final Function<PeerConnectionEntity, Boolean> pcFilterFunc = this.makePredicate(allPCsMatch, anyPCsMatch);

        Predicate<PeerConnectionEntity> pcFilter;

        if (Objects.nonNull(pcFilterFunc)) {
            pcFilter = pcFilterFunc::apply;
        } else {
            pcFilter = c -> false;
        }

        return SentinelEntity.builder()
                .withSentinelDTO(sentinelConfig)
                .withCallFilter(callFilter)
                .withPCFilter(pcFilter)
                .build();
    }


    private<T> Function<T, Boolean> makePredicate(List<Predicate<T>> allMatches, List<Predicate<T>> anyMatches) {
        Function<T, Boolean> result = null;
        if (0 < allMatches.size() && 0 < anyMatches.size()) {
            result = entity -> allMatches.stream().allMatch(predicate -> {
                try {
                    return predicate.test(entity);
                } catch (Throwable throwable) {
                    getLogger().warn("Exception during predicate evaluation", throwable);
                    return false;
                }
            }) && anyMatches.stream().anyMatch(predicate -> {
                try {
                    return predicate.test(entity);
                } catch (Throwable throwable) {
                    getLogger().warn("Exception during predicate evaluation", throwable);
                    return false;
                }
            });
        } else if (0 < allMatches.size()) {
            result = entity -> allMatches.stream().allMatch(predicate -> {
                try {
                    return predicate.test(entity);
                } catch (Throwable throwable) {
                    getLogger().warn("Exception during predicate evaluation", throwable);
                    return false;
                }
            });
        } else if (0 < anyMatches.size()) {
            result = entity -> anyMatches.stream().anyMatch(predicate -> {
                try {
                    return predicate.test(entity);
                } catch (Throwable throwable) {

                    return false;
                }
            });
        }
        if (Objects.isNull(result)) {
            return null;
        }
        return result;
    }

    private Predicate<CallEntity> makeCallFilter(String filterName) {
        CallFilterConfig filterDTO = this.callFilterDTOs.get(filterName);
        if (Objects.isNull(filterDTO)) {
            getLogger().warn("Cannot find filter {}", filterName);
            return c -> true;
        }
        try {
            return this.callFilterBuilder.apply(filterDTO);
        } catch (Throwable throwable) {
            getLogger().warn("Cannot make filter {}", filterName, throwable);
            return c -> true;
        }
    }

    private Predicate<PeerConnectionEntity> makePCFilter(String filterName) {
        PeerConnectionFilterConfig filterDTO = this.pcFilterDTOs.get(filterName);
        if (Objects.isNull(filterDTO)) {
            getLogger().warn("Cannot find filter {}", filterName);
            return c -> true;
        }
        try {
            return this.peerConnectionFilterBuilder.apply(filterDTO);
        } catch (Throwable throwable) {
            getLogger().warn("Cannot make filter {}", filterName, throwable);
            return c -> true;
        }
    }

    @Override
    protected void validate() {

    }


    public FetchSentinelEntitiesTask whereSentinelNames(String... names) {
        if (Objects.isNull(names) || names.length < 1) {
            return this;
        }
        this.sentinelNames.addAll(Arrays.asList(names));
        return this;
    }

    public FetchSentinelEntitiesTask whereSentinelNames(Set<String> names) {
        if (Objects.isNull(names) || names.size() < 1) {
            return this;
        }
        this.sentinelNames.addAll(names);
        return this;
    }
}
