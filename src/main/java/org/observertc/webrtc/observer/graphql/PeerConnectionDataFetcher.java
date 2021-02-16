package org.observertc.webrtc.observer.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.observertc.webrtc.observer.entities.OldPeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.stores.PeerConnectionsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class PeerConnectionDataFetcher implements DataFetcher<Collection<OldPeerConnectionEntity>> {

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Override
    public Collection<OldPeerConnectionEntity> get(DataFetchingEnvironment env) {
        return this.peerConnectionsRepository.getAllEntries().values();
//        String name = env.getArgument("name");
//        if (name == null || name.trim().length() == 0) {
//            name = "World";
//        }
//        return String.format("Hello %s!", name);
    }
}
