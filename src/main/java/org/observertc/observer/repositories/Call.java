package org.observertc.observer.repositories;

import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Call {

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

    public Long getSampleTouch() {
        var model = this.modelHolder.get();
        if (!model.hasSampleTouched()) {
            return null;
        }
        return model.getSampleTouched();
    }

    public Long getServerTouch() {
        var model = this.modelHolder.get();
        if (!model.hasServerTouched()) {
            return null;
        }
        return model.getServerTouched();
    }

    public boolean hasClient(String clientId) {
        var model = this.modelHolder.get();
        if (model.getClientIdsCount() < 1) {
            return false;
        }
        var clientIds = model.getClientIdsList();
        return clientIds.contains(clientId);
    }

    public Set<String> getClientIds() {
        var model = this.modelHolder.get();
        if (model.getClientIdsCount() < 1) {
            return Collections.emptySet();
        }
        return model.getClientIdsList()
                .stream()
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
        var newModel = Models.Call.newBuilder(model)
                .addClientIds(clientId)
                .build();

        this.updateModel(newModel);
        this.clientsRepo.update(clientModel);
        return this.clientsRepo.wrapClient(clientModel);
    }

    public boolean removeClient(String clientId) {
        var clientIds = this.getClientIds();
        if (!clientIds.contains(clientId)) {
            return false;
        }

        var newClientIds = clientIds.stream().filter(actualClientId -> actualClientId != clientId)
                .collect(Collectors.toSet());

        var newModel = Models.Call.newBuilder(this.modelHolder.get())
                .clearClientIds()
                .addAllClientIds(newClientIds)
                .build();

        this.updateModel(newModel);
        this.clientsRepo.delete(clientId);
        return true;
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public void touchBySample(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.Call.newBuilder(model)
                .setSampleTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public void touchByServer(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.Call.newBuilder(model)
                .setServerTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public Models.Call getModel() {
        return this.modelHolder.get();
    }

    private void updateModel(Models.Call newModel) {
        this.modelHolder.set(newModel);
        this.callsRepositoryRepo.update(newModel);
    }


    @Override
    public String toString() {
        return this.modelHolder.get().toString();
    }

}
