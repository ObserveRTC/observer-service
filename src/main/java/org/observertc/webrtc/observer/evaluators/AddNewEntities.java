package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.repositories.tasks.RefreshTask;
import org.observertc.webrtc.observer.samples.CollectedCallSamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Prototype
public class AddNewEntities implements Consumer<CollectedCallSamples> {
    private static final Logger logger = LoggerFactory.getLogger(AddNewEntities.class);

    @Inject
    Provider<RefreshTask> refreshTaskProvider;


    @Override
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        RefreshTask refreshTask = refreshTaskProvider.get();

        RefreshTask.Report report;

    }
}
