package org.observertc.observer.repositories;

import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Call {
    static final String CLIENT_JOINED_EVENT_NAME = "joined";
    static final String CLIENT_DETACHED_EVENT_NAME = "detached";

    private final ServiceRoomId serviceRoomId;
    private final AtomicReference<Models.Call> modelHolder;
    private final CallsRepository callsRepositoryRepo;
    private final ClientsRepository clientsRepo;

    Call(Models.Call model, CallsRepository callsRepositoryRepo, ClientsRepository clientsRepo) {
        this.modelHolder = new AtomicReference<>(model);
        this.callsRepositoryRepo = callsRepositoryRepo;
        this.clientsRepo = clientsRepo;
        this.serviceRoomId = ServiceRoomId.make(model.getServiceId(), model.getRoomId());
    }

    public String getServiceId() {
        return this.modelHolder.get().getServiceId();
    }

    public String getRoomId() {
        return this.modelHolder.get().getRoomId();
    }

    public String getCallId() {
        return this.modelHolder.get().getCallId();
    }

    public Long getStarted() {
        return this.modelHolder.get().getStarted();
    }

    public String getMarker() {
        return this.modelHolder.get().getMarker();
    }

    public boolean hasClient(String clientId) {
        var clientLogs = this.modelHolder.get().getClientLogsList();
        if (clientLogs.size() < 1) {
            return false;
        }
        var hasClient = clientLogs.stream()
                .filter(clientLog -> {
                    return clientLog.getClientId().equals(clientId) && clientLog.getEvent().equals(CLIENT_JOINED_EVENT_NAME);
                })
                .findFirst();
        return hasClient.isPresent();
    }

    public Set<String> getClientIds() {
        var clientLogs = this.modelHolder.get().getClientLogsList();
        if (clientLogs.size() < 1) {
            return Collections.emptySet();
        }
        return clientLogs.stream()
                .filter(clientLog -> clientLog.getEvent().equals(CLIENT_JOINED_EVENT_NAME))
                .map(clientLog -> clientLog.getClientId())
                .collect(Collectors.toSet());
    }

    public Map<String, Client> getClients() {
        var clientIds = this.getClientIds();
        if (clientIds.size() < 1) {
            return Collections.emptyMap();
        }
        return this.clientsRepo.getAll(clientIds);
    }

    public Client getClient(String clientId) {
        var clientIds = this.getClientIds();
        if (clientIds.size() < 1) {
            return null;
        }
        if (!clientIds.contains(clientId)) {
            return null;
        }
        return this.clientsRepo.get(clientId);
    }

    public Client addClient(String clientId, String userId, String mediaUnitId, String timeZoneId, Long timestamp, String marker) throws AlreadyCreatedException {
        var clientIds = this.getClientIds();
        if (clientIds.contains(clientId)) {
            throw AlreadyCreatedException.wrapClientId(clientId);
        }
        var model = this.modelHolder.get();
        this.updateClientLog(clientId, CLIENT_JOINED_EVENT_NAME);

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
        this.clientsRepo.update(clientModel);
        return this.clientsRepo.wrapClient(clientModel);
    }

    public boolean removeClient(String clientId) {
        var clientIds = this.getClientIds();
        if (!clientIds.contains(clientId)) {
            return false;
        }
        this.updateClientLog(clientId, CLIENT_DETACHED_EVENT_NAME);
        this.clientsRepo.delete(clientId);
        return true;
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public Models.Call getModel() {
        return this.modelHolder.get();
    }

    private void updateModel(Models.Call newModel) {
        this.modelHolder.set(newModel);
        this.callsRepositoryRepo.update(newModel);
    }

    private void updateClientLog(String clientId, String event) {
        var clientLogMap = new HashMap<String, Models.Call.ClientLog>();
        var model = this.modelHolder.get();
        if (0 < model.getClientLogsCount()) {
            var clientLogs = model.getClientLogsList();
            for (var clientLog : clientLogs) {
                clientLogMap.put(clientLog.getClientId(), clientLog);
            }
        }

        clientLogMap.put(clientId, Models.Call.ClientLog.newBuilder()
                .setClientId(clientId)
                .setTimestamp(Instant.now().toEpochMilli())
                .setEvent(event)
                .build());
        var newCallModel = Models.Call.newBuilder(model)
                .clearClientLogs()
                .addAllClientLogs(clientLogMap.values())
                .build();
        this.updateModel(newCallModel);
    }

    @Override
    public String toString() {
        return this.modelHolder.get().toString();
    }

}
