package org.observertc.observer;

import io.micronaut.context.BeanProvider;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.repositories.CallsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Singleton
public class Sandbox {

    private static final Logger logger = LoggerFactory.getLogger(Sandbox.class);

    @Inject
    BeanProvider<CallsRepository> callsRepository;

    void start() {
        Schedulers.newThread().schedulePeriodicallyDirect(() -> {
            logger.warn("Sandbox scheduled process");
            this.callsRepository.get().getAll(Set.of("non-existing-call"));
        }, 10000, 10000, TimeUnit.MILLISECONDS);
    }
}
