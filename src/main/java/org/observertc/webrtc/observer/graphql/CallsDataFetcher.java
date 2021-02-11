package org.observertc.webrtc.observer.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.observertc.webrtc.observer.entities.OldCallEntity;
import org.observertc.webrtc.observer.repositories.stores.CallEntitiesRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class CallsDataFetcher implements DataFetcher<Collection<OldCallEntity>> {

    @Inject
    CallEntitiesRepository callEntitiesRepository;

    @Override
    public Collection<OldCallEntity> get(DataFetchingEnvironment env) {
        return this.callEntitiesRepository.getAllEntries().values();
    }
}


