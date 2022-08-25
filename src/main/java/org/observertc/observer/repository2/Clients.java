package org.observertc.observer.repository2;

import io.github.balazskreith.hamok.storagegrid.ReplicatedStorage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.mappings.Codec;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.repositories.Repository;
import org.observertc.schemas.dtos.Models;

import javax.annotation.PostConstruct;

@Singleton
public class Clients {

    private static final String STORAGE_ID = "observertc-clients";

    @Inject
    Repository repository;

    private ReplicatedStorage<String, Models.Client> clients;

    @PostConstruct
    void init() {
        this.clients = this.repository.<String, Models.Call>createReplicatedStorage()
                .setStorageId(STORAGE_ID)
                .setStorage()
                .setKeyCodecSupplier()
                .setValueCodecSupplier(Codec.<Models.Call, byte[]>create(
                        Mapper.create(Models.Call::toByteArray),
                        Mapper.create(bytes -> Models.Call.parseFrom(bytes))
                ))
                .setMaxCollectedStorageEvents()
                .setMaxCollectedStorageTimeInMs()
                .setMaxMessageValues(1000)
                .build();
    }
}
