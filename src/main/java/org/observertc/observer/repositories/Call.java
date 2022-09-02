package org.observertc.observer.repositories;

import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Call {

    private final ServiceRoomId serviceRoomId;
    private final Models.Call model;
    private final CallsRepository callsRepositoryRepo;
    private final ClientsRepository clientsRepo;
    private final CallClientIdsRepository callClientIdsRepo;
    private final AtomicReference<Set<String>> clientIdsHolder;

    Call(Models.Call model, CallsRepository callsRepositoryRepo, ClientsRepository clientsRepo, CallClientIdsRepository callClientIdsRepo, Set<String> clientIds) {
        this.model = model;
        this.callsRepositoryRepo = callsRepositoryRepo;
        this.clientsRepo = clientsRepo;
        this.clientIdsHolder = new AtomicReference<>(clientIds);
        this.callClientIdsRepo = callClientIdsRepo;
        this.serviceRoomId = ServiceRoomId.make(model.getServiceId(), model.getRoomId());
    }

    public String getServiceId() {
        return this.model.getServiceId();
    }

    public String getRoomId() {
        return this.model.getRoomId();
    }

    public String getCallId() {
        return this.model.getCallId();
    }

    public Long getStarted() {
        return this.model.getStarted();
    }

    public String getMarker() {
        return this.model.getMarker();
    }

    public boolean hasClient(String clientId) {
        var clientIds = this.clientIdsHolder.get();
        return clientIds.contains(clientId);
    }

    public Set<String> getClientIds() {
        return this.clientIdsHolder.get();
    }

    public Map<String, Client> getClients() {
        var clientIds = this.clientIdsHolder.get();
        if (clientIds.size() < 1) {
            return Collections.emptyMap();
        }
        return this.clientsRepo.getAll(clientIds);
    }

    public Client getClient(String clientId) {
        var clientIds = this.clientIdsHolder.get();
        if (clientIds.size() < 1) {
            return null;
        }
        if (!clientIds.contains(clientId)) {
            return null;
        }
        return this.clientsRepo.get(clientId);
    }

    public Client addClient(String clientId, String userId, String mediaUnitId, String timeZoneId, Long timestamp) throws AlreadyCreatedException {
        var clientIds = this.clientIdsHolder.get();
        if (clientIds.contains(clientId)) {
            throw AlreadyCreatedException.wrapClientId(clientId);
        }
        var newClientIds = Stream.concat(clientIds.stream(), Stream.of(clientId))
                .collect(Collectors.toSet());

        var clientModel = Models.Client.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setUserId(userId)
                .setTouched(timestamp)
                .setMediaUnitId(mediaUnitId)
                .setJoined(timestamp)
                .setTimeZoneId(timeZoneId)
                .setMarker(model.getMarker())
                .build();
        this.clientIdsHolder.set(newClientIds);
        this.callClientIdsRepo.update(this.model.getCallId(), newClientIds);
        this.clientsRepo.update(clientModel);
        return this.clientsRepo.wrapClient(clientModel);
    }

    public boolean removeClient(String clientId) {
        var clientIds = this.clientIdsHolder.get();
        if (!clientIds.contains(clientId)) {
            return false;
        }
        var newClientIds = clientIds.stream().filter(savedClientId -> savedClientId != clientId)
                .collect(Collectors.toSet());
        this.clientIdsHolder.set(newClientIds);
        this.callClientIdsRepo.delete(clientId);
        this.clientsRepo.delete(clientId);
        return true;
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public Models.Call getModel() {
        return this.model;
    }

}
