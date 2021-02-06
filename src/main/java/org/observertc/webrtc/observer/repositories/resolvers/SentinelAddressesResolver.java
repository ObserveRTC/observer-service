package org.observertc.webrtc.observer.repositories.resolvers;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.repositories.SentinelsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;

@Prototype
public class SentinelAddressesResolver implements Function<String, Optional<String>> {
    private static final Logger logger = LoggerFactory.getLogger(SentinelAddressesResolver.class);

    @Inject
    SentinelsRepository repository;

    private Map<String, String> dictionary;
    private int counter;


    @PostConstruct
    void setup() {

    }

    @Override
    public Optional<String> apply(String address) {
        if (0 == this.counter) {
            this.dictionary = this.fetch();
            logger.info("Sentinels are registered for the following addresses fom config {}", ObjectToString.toString(this.dictionary));
        }
        ++this.counter;
        String result = this.dictionary.get(address);
        if (Objects.isNull(result)) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    private Map<String, String> fetch() {
        Map<String, String> result = new HashMap<>();
        Map<String, SentinelEntity> entries = this.repository.getAllEntries();
        Iterator<SentinelEntity> it = entries.values().iterator();
        while (it.hasNext()) {
            SentinelEntity entity = it.next();
            for (String address : entity.addresses) {
                result.put(address, entity.name);
            }
        }
        return result;
    };
}
