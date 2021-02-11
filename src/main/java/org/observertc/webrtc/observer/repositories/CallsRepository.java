package org.observertc.webrtc.observer.repositories;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.OldCallEntity;
import org.observertc.webrtc.observer.tasks.WeakLockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class CallsRepository {
    private static final Logger logger = LoggerFactory.getLogger(CallsRepository.class);

    private static final String CALL_MODIFICATION_LOCK_NAME = "observertc-add-new-call-lock";
    private static final String REPOSITORY_NAME = "observertc-calls";
    private IMap<UUID, CallDTO> callDTOs;
    private IMap<UUID, PeerConnectionDTO> pcDTOs;

    @Inject
    ObserverHazelcast observerHazelcast;

    @Inject
    WeakLockProvider weakLockProvider;

    @PostConstruct
    public void setup() {
        String callDTOMapName = String.format("%s-callDTOs", REPOSITORY_NAME);
        this.callDTOs = this.observerHazelcast.getInstance().getMap(callDTOMapName);
        String pcDTOMapName = String.format("%s-pcDTOs", REPOSITORY_NAME);
        this.pcDTOs = this.observerHazelcast.getInstance().getMap(pcDTOMapName);
    }

    public Optional<CallDTO> initiateCall(CallDTO callDTO, Set<Long> SSRCs) {
        // TODO: validate CallDTO
        ChainedTask<CallDTO> callAdder = this.makeCallAdder(callDTO, SSRCs);

        if (!callAdder.execute().succeeded()) {
            return Optional.empty();
        }
        CallDTO result = callAdder.getResult();
        if (Objects.isNull(result)) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public Optional<UUID> joinPeerConnection(UUID callUUID, PeerConnectionDTO peerConnectionDTO) {
        throw new IllegalStateException();
    }

    public Optional<PeerConnectionDTO> detachPeerConnection(UUID peerConnectionUUID) {
        throw new IllegalStateException();
    }

    public Optional<CallDTO> finishCall() {
        throw new IllegalStateException();
    }

    public OldCallEntity fetchEntity() {
        throw new IllegalStateException();
    }

    public OldCallEntity fetchCallEntity(boolean fetchPCs, boolean fetchSSRCs) {
        throw new IllegalStateException();
    }

    private IMap<Long, UUID> getSsrcMap(UUID serviceUUID) {
        String mapName = String.format("%s-ssrcMap", REPOSITORY_NAME);
        return this.observerHazelcast.getInstance().getMap(mapName);
    }

    private MultiMap<String, UUID> getCallNameMap(UUID serviceUUID) {
        String mapName = String.format("%s-nameMap", REPOSITORY_NAME);
        return this.observerHazelcast.getInstance().getMultiMap(mapName);
    }

    private ChainedTask<CallDTO> makeCallAdder(CallDTO newCallDTO, Set<Long> SSRCs) {
        ChainedTask<CallDTO> result = ChainedTask.<CallDTO>builder()
                .withLockProvider(() -> weakLockProvider.autoLock(CALL_MODIFICATION_LOCK_NAME))
                .addSupplierStage("Try Find Call By Name", () -> {
                    // tries to look for calls by name.
                    if (Objects.isNull(newCallDTO.callName)) {
                        return null;
                    }
                    Collection<UUID> foundCollection = getCallNameMap(newCallDTO.serviceUUID).get(newCallDTO.callName);

                    if (Objects.isNull(foundCollection)) {
                        return null;
                    }
                    Set<UUID> foundCallUUIDs = foundCollection.stream().collect(Collectors.toSet());
                    if (foundCallUUIDs.size() < 1) {
                        return null;
                    }

                    // if we have found some callUUIDs then we try to retrieve one.
                    UUID foundCallUUID = null;
                    if (1 == foundCallUUIDs.size()) {
                        foundCallUUID = foundCallUUIDs.stream().findFirst().get();
                    } else {
                        if (0 < SSRCs.size()) {
                            logger.info("There are multiple call has found for call name {} in service {}, " +
                                    "but there is SSRC we can use to determine which call it belongs to.");
                            return null;
                        }
                        logger.warn("There are multiple call has found for call name {} in service {}, " +
                                "but there is no SSRC we can use to determine which call it belongs to." +
                                "In this implementation we choose the first available one.", newCallDTO.callName, newCallDTO.serviceUUID);
                        // TODO: maybe mark those calls and exemine it in a cleaner process?
                        foundCallUUID = foundCallUUIDs.stream().findFirst().get();
                    }

                    if (Objects.isNull(foundCallUUID)) {
                        return null;
                    }

                    // we found one callUUID, so let's try to retrieve the callDTO for it
                    CallDTO foundCallDTO = callDTOs.get(foundCallUUID);
                    if (Objects.isNull(foundCallDTO)) {
                        logger.warn("call uuid {} for call name {} does not exists in callDTOs map. Removing the calName now from callNamesMap",
                                foundCallUUID, newCallDTO.callName);
                        getCallNameMap(newCallDTO.serviceUUID).remove(newCallDTO.callName);
                        return null;
                    }
                    return foundCallDTO;
                })

                .addFunctionalStage("Try Find Call By SSRC", prevFoundCallDTO -> {
                    if (Objects.nonNull(prevFoundCallDTO)) { // already found previously
                        return prevFoundCallDTO;
                    }
                    if (Objects.isNull(SSRCs) || SSRCs.size() < 1) { // no ssrc for the call
                        return null;
                    }
                    // tries to look for calls by SSRCs
                    Map<Long, UUID> ssrcToCallUUIDs = getSsrcMap(newCallDTO.serviceUUID).getAll(SSRCs);
                    if (Objects.isNull(ssrcToCallUUIDs) || ssrcToCallUUIDs.size() < 1) {
                        return null;
                    }
                    Set<UUID> foundCallUUIDs = ssrcToCallUUIDs.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
                    if (Objects.isNull(foundCallUUIDs) || foundCallUUIDs.size() < 1) {
                        return null;
                    }

                    // we may found some, but than we have the same problem as with the callUUIDs
                    UUID callUUID = null;
                    if (1 == foundCallUUIDs.size()) {
                        callUUID = foundCallUUIDs.stream().findFirst().get();
                    } else {
                        logger.warn("There are more than one call uuid found ({}) for SSRCs ({}). That's pretty creepy." +
                                        "In the current implementation we choose the first one.",
                                ObjectToString.toString(foundCallUUIDs), ObjectToString.toString(SSRCs));
                        callUUID = foundCallUUIDs.stream().findFirst().get();
                    }
                    if (Objects.isNull(callUUID)) {
                        return null;
                    }
                    CallDTO foundCallDTO = callDTOs.get(callUUID);
                    if (Objects.isNull(foundCallDTO)) {
                        logger.warn("call uuid {} for SSRCs {} does not exists in callDTOs map. Removing the SSRCs now from ssrcMap",
                                callUUID, ObjectToString.toString(SSRCs));
                        IMap<Long, UUID> ssrcMap = getSsrcMap(newCallDTO.serviceUUID);
                        SSRCs.forEach(ssrcMap::remove);
                        return null;
                    }
                    return foundCallDTO;
                })
                .addFunctionalStage("Add new Call",
                    foundCallDTOObj -> {
                        if (Objects.nonNull(foundCallDTOObj)) {
                            return foundCallDTOObj;
                        }
                        callDTOs.put(newCallDTO.callUUID, newCallDTO);
                        return null;
                    },(foundCallDTOHolder, thrown) -> {
                        if (Objects.isNull(foundCallDTOHolder)) {
                            return;
                        }
                        CallDTO foundCallDTO = (CallDTO) foundCallDTOHolder.get();
                        if (Objects.nonNull(foundCallDTO)) {
                            return;
                        }
                        callDTOs.remove(newCallDTO.callUUID);
                })
                .addFunctionalStage("Add Call Name",
                        foundCallDTOObj -> {
                            if (Objects.nonNull(foundCallDTOObj)) {
                                return foundCallDTOObj;
                            }
                            if (Objects.isNull(newCallDTO.callName)) {
                                return null;
                            }
                            getCallNameMap(newCallDTO.serviceUUID).put(newCallDTO.callName, newCallDTO.callUUID);
                            return null;
                        },(foundCallDtoObj, thrown) -> {
                            if (Objects.nonNull(foundCallDtoObj)) {
                                return;
                            }
                            if (Objects.isNull(newCallDTO.callName)) {
                                return;
                            }
                            getCallNameMap(newCallDTO.serviceUUID).remove(newCallDTO.callName, newCallDTO.callUUID);
                })
                .addTerminalCondition("Add Call SSRC",
                        foundCallDTOObj -> {
                            if (Objects.nonNull(foundCallDTOObj)) {
                                return foundCallDTOObj;
                            }
                            if (Objects.isNull(SSRCs) || SSRCs.size() < 1) {
                                return newCallDTO;
                            }
                            Map<Long, UUID> ssrmap = SSRCs.stream().collect(Collectors.toMap(Function.identity(), s -> newCallDTO.callUUID));
                            getSsrcMap(newCallDTO.serviceUUID).putAll(ssrmap);
                            return newCallDTO;
                        },(foundCallDtoObj, thrown) -> {
                            if (Objects.nonNull(foundCallDtoObj)) {
                                return;
                            }
                            if (Objects.isNull(SSRCs) || SSRCs.size() < 1) {
                                return;
                            }

                            IMap<Long, UUID> ssrcMap = getSsrcMap(newCallDTO.serviceUUID);
                            SSRCs.forEach(ssrcMap::remove);
                })
                .build();
        return result;
    }

}
