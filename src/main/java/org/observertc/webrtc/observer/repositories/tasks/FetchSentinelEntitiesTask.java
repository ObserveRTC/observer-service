package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.SentinelDTO;
import org.observertc.webrtc.observer.dto.SentinelFilterDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.sentinels.SentinelFilterBuilder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FetchSentinelEntitiesTask extends ChainedTask<Map<String, SentinelEntity>> {

    private Set<String> sentinelNames = new HashSet<>();
    private Map<String, SentinelDTO> sentinelDTOs = null;
    private Map<String, SentinelFilterDTO> filterDTOs = null;

    @Inject
    SentinelFilterBuilder sentinelFilterBuilder;

    @Inject
    HazelcastMaps hazelcastMaps;


    @PostConstruct
    void setup() {
        new Builder<Map<String, SentinelEntity>>(this)
            .addActionStage("Fetch SentinelDTOs",
                () -> {
                    this.sentinelDTOs = hazelcastMaps.getSentinelDTOs().getAll(this.sentinelNames);
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
                    this.sentinelDTOs.values().stream().filter(s -> Objects.nonNull(s.anyMatchFilters)).flatMap(s -> Arrays.stream(s.anyMatchFilters)).forEach(filterNames::add);
                    this.sentinelDTOs.values().stream().filter(s -> Objects.nonNull(s.allMatchFilters)).flatMap(s -> Arrays.stream(s.allMatchFilters)).forEach(filterNames::add);
                    this.filterDTOs = this.hazelcastMaps.getSentinelFilterDTOs().getAll(filterNames);
                })
            .addBreakCondition(resultHolder -> {
                if (Objects.isNull(this.filterDTOs) || this.filterDTOs.size() < 1) {
                    getLogger().info("No Filter has been added for sentinels {}", this.sentinelDTOs);
                    resultHolder.set(Collections.EMPTY_MAP);
                    return true;
                }
                return false;
            })
            .addTerminalSupplier("Build Sentinel Entities", () -> {
                Map<String, SentinelEntity> result = new HashMap<>();
                for (SentinelDTO sentinelDTO : this.sentinelDTOs.values()) {
                    SentinelEntity sentinelEntity = this.makeSentinelEntity(sentinelDTO);
                    result.put(sentinelEntity.getName(), sentinelEntity);
                }
                return Collections.unmodifiableMap(result);
            })
        .build();
    }

    private SentinelEntity makeSentinelEntity(SentinelDTO sentinelDTO) {
        if (Objects.isNull(sentinelDTO)) {
            return null;
        }
        if ((Objects.isNull(sentinelDTO.anyMatchFilters) || sentinelDTO.anyMatchFilters.length < 1) &&
            (Objects.isNull(sentinelDTO.allMatchFilters) || sentinelDTO.allMatchFilters.length < 1)) {
            getLogger().warn("Sentinel {} does not have any filter configured");
            return null;
        }

        Predicate<CallEntity> filter;
        List<Predicate<CallEntity>> allMatches = new LinkedList<>();
        List<Predicate<CallEntity>> anyMatches = new LinkedList<>();
        if (Objects.nonNull(sentinelDTO.anyMatchFilters)) {
            Arrays.stream(sentinelDTO.anyMatchFilters).forEach(filterName -> addSentinelFilter(anyMatches, filterName));
        }
        if (Objects.nonNull(sentinelDTO.allMatchFilters)) {
            Arrays.stream(sentinelDTO.allMatchFilters).forEach(filterName -> addSentinelFilter(allMatches, filterName));
        }

        if (0 < allMatches.size() && 0 < anyMatches.size()) {
            filter = callEntity -> allMatches.stream().allMatch(predicate -> {
                try {
                    return predicate.test(callEntity);
                } catch (Throwable throwable) {
                    getLogger().warn("Exception during predicate evaluation", throwable);
                    return false;
                }
            }) && anyMatches.stream().anyMatch(predicate -> {
                try {
                    return predicate.test(callEntity);
                } catch (Throwable throwable) {
                    getLogger().warn("Exception during predicate evaluation", throwable);
                    return false;
                }
            });
        } else if (0 < allMatches.size()) {
            filter = callEntity -> allMatches.stream().allMatch(predicate -> {
                try {
                    return predicate.test(callEntity);
                } catch (Throwable throwable) {
                    getLogger().warn("Exception during predicate evaluation", throwable);
                    return false;
                }
            });
        } else if (0 < anyMatches.size()) {
            filter = callEntity -> anyMatches.stream().anyMatch(predicate -> {
                try {
                    return predicate.test(callEntity);
                } catch (Throwable throwable) {
                    getLogger().warn("Exception during predicate evaluation", throwable);
                    return false;
                }
            });
        } else {
            getLogger().warn("No filter has been defined for sentinel {}", sentinelDTO.name);
            filter = callEntity -> false;
        }

        return SentinelEntity.builder()
                .withSentinelDTO(sentinelDTO)
                .withFilter(filter)
                .build();
    }

    private void addSentinelFilter(List<Predicate<CallEntity>> target, String filterName) {
        SentinelFilterDTO filterDTO = this.filterDTOs.get(filterName);
        if (Objects.isNull(filterDTO)) {
            getLogger().warn("Cannot find filter {}", filterName);
            return;
        }
        Predicate<CallEntity> filter;
        try {
            filter = this.sentinelFilterBuilder.apply(filterDTO);
        } catch (Throwable throwable) {
            getLogger().warn("Cannot make filter {}", filterName, throwable);
            return;
        }
        target.add(filter);
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
