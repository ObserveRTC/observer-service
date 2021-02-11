package org.observertc.webrtc.observer.repositories.resolvers;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.repositories.stores.SentinelsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@Prototype
public class SentinelCallParticipantsResolver implements BiFunction<String, Integer, Optional<String>> {
    private static final Logger logger = LoggerFactory.getLogger(SentinelCallParticipantsResolver.class);

    @Inject
    SentinelsRepository repository;

    private Map<String, List<BiPredicate<String, Integer>>> predicators = new HashMap<>();
    private int counter = 0;

    @PostConstruct
    void setup() {

    }

    @Override
    public Optional<String> apply(String serviceName, Integer participantNumber) {
        if (++this.counter == 1) {
            this.predicators = this.fetch();
            logger.info("Sentinels are fetched for callFilters");
        }
        Iterator<Map.Entry<String, List<BiPredicate<String, Integer>>>> it = this.predicators.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<BiPredicate<String, Integer>>> entry = it.next();
            String sentinelName = entry.getKey();
            List<BiPredicate<String, Integer>> testers = entry.getValue();
            if (Objects.isNull(testers)) {
                continue;
            }
            for (BiPredicate<String, Integer> tester : testers) {
                if (tester.test(serviceName, participantNumber)) {
                    return Optional.of(sentinelName);
                }
            }
        }
        return Optional.empty();
    }

    private Map<String, List<BiPredicate<String, Integer>>> fetch() {
        Map<String, List<BiPredicate<String, Integer>>> result = new HashMap<>();
        Map<String, SentinelEntity> entries = this.repository.getAllEntries();
        Iterator<SentinelEntity> it = entries.values().iterator();
        while (it.hasNext()) {
            SentinelEntity entity = it.next();
            for (String callFilter : entity.callFilters) {
                String[] parts = callFilter.split(" ", 2);
                if (Objects.isNull(parts) || parts.length < 1) {
                    continue;
                }
                List<BiPredicate<String, Integer>> testers = result.get(entity.name);
                if (Objects.isNull(testers)) {
                    testers = new LinkedList<>();
                    result.put(entity.name, testers);
                }
                if (parts.length < 2) {
                    testers.add(new BiPredicate<String, Integer>() {
                        @Override
                        public boolean test(String serviceName, Integer integer) {
                            return parts[0].equals(serviceName);
                        }
                    });
                    continue;
                }
                try {
                    int minParticipantNum = Integer.valueOf(parts[1]);
                    testers.add(new BiPredicate<String, Integer>() {
                        @Override
                        public boolean test(String serviceName, Integer participantsNum) {
                            return parts[0].equals(serviceName) &&  minParticipantNum <= participantsNum;
                        }
                    });
                    logger.info("Sentinel filter for {} is registered. serviceName: {}, number of minimum participant is {}", entity.name, parts[0], minParticipantNum);
                }catch (Throwable t) {
                    logger.warn("Exception during fetching ", t);
                }
            }
        }
        return result;
    };


}
