package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.storagegrid.FederatedStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.ReplicatedStorage;
import io.github.balazskreith.hamok.storagegrid.ReplicatedStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorageBuilder;
import jakarta.inject.Singleton;
import org.observertc.schemas.dtos.Models;

@Singleton
class Repository {


    public ReplicatedStorage<String, Models.Call> getCalls() {

    }

    public<K, V> ReplicatedStorageBuilder<K, V> createReplicatedStorage() {

    }

    public<K, V> SeparatedStorageBuilder<K, V> createSeparatedStorage() {

    }

    public<K, V>FederatedStorageBuilder<K, V> createFederatedStorage() {

    }
}
