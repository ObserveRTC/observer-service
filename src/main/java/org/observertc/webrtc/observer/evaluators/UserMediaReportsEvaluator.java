package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.repositories.hazelcast.UserMediaErrorsRepository;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.observertc.webrtc.observer.tasks.UserMediaErrorsUpdaterTask;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.UserMediaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class UserMediaReportsEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(UserMediaReportsEvaluator.class);

    private Subject<Report> userMediaErrorReports = PublishSubject.create();

    @Inject
    TasksProvider tasksProvider;

    @PostConstruct
    void setup() {
        this.userMediaErrorReports
                .buffer(10, TimeUnit.SECONDS, 1000)
                .subscribe(this::process);
    }


    public Observer<Report> getInput() {
        return this.userMediaErrorReports;
    }

    private void process(List<Report> reports) {
        if (reports.size() < 1) {
            return;
        }
        Map<String, Integer> trackedErrors = new HashMap<>();
        for (Report report : reports) {
            UserMediaError userMediaError = (UserMediaError) report.getPayload();
            if (Objects.isNull(userMediaError)) {
                logger.warn("Report {} payload is null, it cannot be tracked", report.toString());
                continue;
            }
            String mediaUnitId = userMediaError.getMediaUnitId();
            if (Objects.isNull(mediaUnitId)) {
                logger.warn("Report {} mediaunitId is null, it cannot be tracked", report.toString());
                continue;
            }
            if (Objects.isNull(report.getServiceUUID())) {
                logger.warn("Report {} serviceUUID is null, it cannot be tracked", report.toString());
                continue;
            }
            UUID serviceUUID = UUID.fromString(report.getServiceUUID());
            String key = UserMediaErrorsRepository.getKey(serviceUUID, mediaUnitId);
            Integer tracked = trackedErrors.getOrDefault(key, 0);
            trackedErrors.put(key,  ++tracked);
        }

        UserMediaErrorsUpdaterTask task = this.tasksProvider.getUserMediaErrorUpdaterTask();
        task.withTrackedErrors(trackedErrors).withLogger(logger).execute();

        if (!task.succeeded()) {
            logger.warn("{} is failed", task.getClass().getSimpleName());
        }
    }
}
