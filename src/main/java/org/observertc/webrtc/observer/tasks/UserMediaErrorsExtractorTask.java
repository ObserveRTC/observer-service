package org.observertc.webrtc.observer.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.UserMediaErrorsRepository;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Prototype
public class UserMediaErrorsExtractorTask extends TaskAbstract<Map<String, Integer>> {
    private enum State {
        CREATED,
        COLLECTED,
        REMOVED,
        EXECUTED,
        ROLLBACK
    }
    private State state = State.CREATED;
    private Map<String, Integer> locallyStoredEntries = new HashMap<>();

    @Inject
    RepositoryProvider repositoryProvider;

    @Override
    protected Map<String, Integer> perform() throws Throwable {
        UserMediaErrorsRepository repository = this.repositoryProvider.getUserMediaErrorsRepository();
        switch (this.state) {
            default:
            case CREATED:
                this.locallyStoredEntries = repository.getLocalEntries();
                this.state = State.COLLECTED;
            case COLLECTED:
                repository.removeAll(this.locallyStoredEntries.keySet());
                this.state = State.REMOVED;
            case REMOVED:
                this.state = State.EXECUTED;
            case EXECUTED:
            case ROLLBACK:
                break;
        }
        return this.locallyStoredEntries;
    }

    @Override
    protected void rollback(Throwable t) {
        UserMediaErrorsRepository repository = this.repositoryProvider.getUserMediaErrorsRepository();
        switch (this.state) {
            case EXECUTED:
                this.state = State.REMOVED;
            case REMOVED:
                if (Objects.nonNull(this.locallyStoredEntries)) {
                    repository.saveAll(this.locallyStoredEntries);
                }
            case COLLECTED:
            case CREATED:
                this.state = State.ROLLBACK;
            case ROLLBACK:
                break;
        }
    }
}
