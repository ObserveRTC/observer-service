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
    private final CallClientIdsRepository callClientIdsRepository;
    private final ClientsRepository clientsRepo;
    private final AtomicReference<Set<String>> clientIdsHolder;

    Call(Models.Call model, CallsRepository callsRepositoryRepo, ClientsRepository clientsRepo, CallClientIdsRepository callClientIdsRepository, Set<String> clientIds) {
        this.model = model;
        this.callsRepositoryRepo = callsRepositoryRepo;
        this.clientsRepo = clientsRepo;
        this.clientIdsHolder = new AtomicReference<>(clientIds);
        this.callClientIdsRepository = callClientIdsRepository;
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

    public Client addClient(String clientId, String userId, String mediaUnitId, String timeZoneId, Long timestamp, String marker) throws AlreadyCreatedException {
        var clientIds = this.clientIdsHolder.get();
        if (clientIds.contains(clientId)) {
            throw AlreadyCreatedException.wrapClientId(clientId);
        }
        var newClientIds = Stream.concat(clientIds.stream(), Stream.of(clientId))
                .collect(Collectors.toSet());

        var clientModelBuilder = Models.Client.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setClientId(clientId)
                .setJoined(timestamp)
                .setTouched(timestamp)
                // timeZoneId
                .setMediaUnitId(mediaUnitId)
                // userId
                // marker
                ;
        if (userId != null) {
            clientModelBuilder.setUserId(userId);
        }
        if (timeZoneId != null) {
            clientModelBuilder.setTimeZoneId(timeZoneId);
        }
        if (marker != null) {
            clientModelBuilder.setMarker(marker);
        }
        var clientModel = clientModelBuilder.build();
        this.clientIdsHolder.set(newClientIds);
        this.callClientIdsRepository.add(this.model.getCallId(), clientId);
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
        this.callClientIdsRepository.delete(this.model.getCallId(), clientId);
        this.clientsRepo.delete(clientId);
        return true;
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public Models.Call getModel() {
        return this.model;
    }

    @Override
    public String toString() {
        return this.model.toString();
    }

}
