package org.observertc.webrtc.observer.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class PeerConnectionDataFetcher implements DataFetcher<Collection<PeerConnectionEntity>> {

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Override
    public Collection<PeerConnectionEntity> get(DataFetchingEnvironment env) {
        return this.peerConnectionsRepository.getAllEntries().values();
//        String name = env.getArgument("name");
//        if (name == null || name.trim().length() == 0) {
//            name = "World";
//        }
//        return String.format("Hello %s!", name);
    }
}
