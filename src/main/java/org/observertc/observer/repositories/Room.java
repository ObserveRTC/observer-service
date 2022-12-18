package org.observertc.observer.repositories;

import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class Room {

    private final ServiceRoomId serviceRoomId;
    private final AtomicReference<Models.Room> modelHolder;
    private final RoomsRepository roomsRepository;
    private final CallsRepository callsRepositoryRepo;

    Room(
            Models.Room model,
            RoomsRepository roomsRepository,
            CallsRepository callsRepositoryRepo
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.roomsRepository = roomsRepository;
        this.callsRepositoryRepo = callsRepositoryRepo;
        this.serviceRoomId = ServiceRoomId.make(model.getServiceId(), model.getRoomId());
    }

    public String getServiceId() {
        return this.modelHolder.get().getServiceId();
    }

    public String getRoomId() {
        return this.modelHolder.get().getRoomId();
    }

    public String getCallId() {
        var model = this.modelHolder.get();
        if (model == null || !model.hasCallId()) {
            return null;
        }
        return model.getCallId();
    }

    public Call getCall() {
        var model = this.modelHolder.get();
        if (model == null || !model.hasCallId()) {
            return null;
        }
        var callId = model.getCallId();
        return this.callsRepositoryRepo.get(callId);
    }

    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    public Models.Room getModel() {
        return this.modelHolder.get();
    }

    private void updateModel(Models.Room newModel) {
        this.modelHolder.set(newModel);
        this.roomsRepository.update(newModel);
    }

    @Override
    public String toString() {
        return this.modelHolder.get().toString();
    }


}
