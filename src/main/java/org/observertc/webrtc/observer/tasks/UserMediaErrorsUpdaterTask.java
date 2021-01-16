package org.observertc.webrtc.observer.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.UserMediaErrorsRepository;

import javax.inject.Inject;
import java.util.*;

@Prototype
public class UserMediaErrorsUpdaterTask extends TaskAbstract<Void> {

    private final Map<String, Integer> trackedErrors = new HashMap<>();

    @Inject
    RepositoryProvider repositoryProvider;

    public UserMediaErrorsUpdaterTask withTrackedErrors(Map<String, Integer> trackedErrors) {
        if (Objects.isNull(trackedErrors) || trackedErrors.size() < 1) {
            return this;
        }
        this.accumulate(trackedErrors, this.trackedErrors);
        return this;
    }

    @Override
    protected Void perform() throws Throwable {
        if (this.trackedErrors.size() < 1) {
            return null;
        }
        UserMediaErrorsRepository userMediaErrorsRepository = this.repositoryProvider.getUserMediaErrorsRepository();
        Set<String> keys = this.trackedErrors.keySet();
        Map<String, Integer> stored = userMediaErrorsRepository.findAll(keys);
        userMediaErrorsRepository.saveAll(this.accumulate(this.trackedErrors, stored));
        return null;
    }

    @Override
    protected void rollback(Throwable t) {
        // no need to rollback any operation here
    }

    private Map<String, Integer> accumulate(Map<String, Integer> source, Map<String, Integer> target) {
        Iterator<Map.Entry<String, Integer>> it = source.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            Integer tracked = target.getOrDefault(entry.getKey(), 0);
            tracked += entry.getValue();
            target.put(entry.getKey(), tracked);
        }
        return target;
    }
}
