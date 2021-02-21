package org.observertc.webrtc.observer.repositories.tasks;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FetchPCsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private Set<UUID> pcUUIDs = new HashSet<>();

    @Inject
    HazelcastMaps hazelcastMaps;
    private boolean fetchSSRCs = true;


    @PostConstruct
    void setup() {
        Function<Set<UUID>, Map<UUID, PeerConnectionDTO>> fetchPcDTO = pcUUIDs -> {
            if (Objects.nonNull(pcUUIDs)) {
                this.pcUUIDs.addAll(pcUUIDs);
            }
            if (this.pcUUIDs.size() < 1) {
                return Collections.EMPTY_MAP;
            }
            return hazelcastMaps.getPcDTOs().getAll(this.pcUUIDs);
        };
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
            .<Set<UUID>, Map<UUID, PeerConnectionDTO>>addSupplierEntry("Find PC DTO By UUID",
                    () -> fetchPcDTO.apply(this.pcUUIDs),
                    pcUUID -> fetchPcDTO.apply(pcUUID)
            )
            .<Map<UUID, PeerConnectionDTO>>addBreakCondition((pcDTOMap, resultHolder) -> {
                if (Objects.isNull(pcDTOMap) || pcDTOMap.size() < 1) {
                    resultHolder.set(Collections.EMPTY_MAP);
                    return true;
                }
                return false;
            })
            .<Map<UUID, PeerConnectionDTO>, Map<UUID, PeerConnectionEntity.Builder>> addFunctionalStage("Convert pcDTO to pc entity builder", pcDTOMap -> {
                Map<UUID, PeerConnectionEntity.Builder> pcBuilders = pcDTOMap.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            PeerConnectionEntity.Builder builder = new PeerConnectionEntity.Builder();
                            builder.pcDTO = entry.getValue();
                            return builder;
                        }
                ));
                return pcBuilders;
            })
            .<Map<UUID, PeerConnectionEntity.Builder>, Map<UUID, PeerConnectionEntity.Builder>> addFunctionalStage("Add SSRCs",
                pcEntityBuilders -> {
                    if (!this.fetchSSRCs) {
                        return pcEntityBuilders;
                    }
                    for (PeerConnectionEntity.Builder pcEntityBuilder : pcEntityBuilders.values()) {
                        Collection<Long> SSRCs = hazelcastMaps.getPCsToSSRCMap(pcEntityBuilder.pcDTO.serviceUUID).get(pcEntityBuilder.pcDTO.peerConnectionUUID);
                        pcEntityBuilder.SSRCs.addAll(SSRCs);
                    }
                    return pcEntityBuilders;
                }
            )
            .<Map<UUID, PeerConnectionEntity.Builder>> addTerminalFunction("Creating Peer Connection Entities", dataCarriers -> {
                return dataCarriers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    PeerConnectionEntity.Builder pcEntityBuilder = entry.getValue();
                    return pcEntityBuilder.build();
                }));
            })
        .build();
    }

    public FetchPCsTask wherePCUuid(UUID... values) {
        if (Objects.isNull(values) || values.length < 1) {
            return this;
        }
        this.pcUUIDs.addAll(Arrays.asList(values));
        return this;
    }

    public FetchPCsTask wherePCUuid(Set<UUID> pcUUIDs) {
        if (Objects.isNull(pcUUIDs) || pcUUIDs.size() < 1) {
            return this;
        }
        this.pcUUIDs.addAll(pcUUIDs);
        return this;
    }

    public FetchPCsTask doNotFetchSSRCs() {
        this.fetchSSRCs = false;
        return this;
    }

    @Override
    protected void validate() {

    }


}
