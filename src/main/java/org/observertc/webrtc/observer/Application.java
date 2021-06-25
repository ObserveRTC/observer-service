/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer;

import com.hazelcast.collection.ISet;
import io.dekorate.prometheus.annotation.EnableServiceMonitor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.observertc.webrtc.observer.common.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;


@TypeHint(
        typeNames = {
                "io.micronaut.caffeine.cache.SSAW",
                "io.micronaut.caffeine.cache.PSAW"
        },
        accessType = {
                TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
                TypeHint.AccessType.ALL_PUBLIC_METHODS,
                TypeHint.AccessType.ALL_DECLARED_FIELDS
        }
)
@OpenAPIDefinition(
        info = @Info(
                title = "observer",
                version = "H"
        )
)
@EnableServiceMonitor(port = "http", path = "/prometheus")
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String INITIAL_WAIT_IN_S = "INITIAL_WAITING_TIME_IN_S";
    public static ApplicationContext context;

    public static void main(String[] args) {
        Sleeper.makeFromSystemEnv(INITIAL_WAIT_IN_S, ChronoUnit.SECONDS).run();
        context = Micronaut.run(Application.class);
        // To make the pipeline run!
        ObserverHazelcast observerHazelcast = context.getBean(ObserverHazelcast.class);
        logger.info("Hazelcast configuration: {}", observerHazelcast.toString());
        deployCheck(observerHazelcast);
        renderLogo();
    }

    private static void renderLogo() {
        URL url = ClassLoader.getSystemResource("logo.txt");
        try {
            var path = Path.of(url.getPath());
            List<String> lines = Files.readAllLines(path);
            var text = String.join("\n", lines);
            System.out.println(text);
        } catch (Throwable t) {
            logger.error("Error rendering logo", t);
        }
    }

    /**
     * Check if the hazelcast contains a value previously
     * added to the cluster automatically whenever this application was part.
     *
     * @param observerHazelcast
     */
    static void deployCheck(ObserverHazelcast observerHazelcast) {
        ISet<String> checkSet = observerHazelcast.getInstance().getSet(Application.class.getSimpleName());
        // Some random sleep, so everyone gets to the point at a bit different time.
        new Sleeper(() -> new Random().nextInt(10) * 1000).run();
        if (!checkSet.contains(Application.class.getSimpleName())) {
            logger.info("It seems this is the first time the application is deployed. " +
                    "If you have done RollingUpdate, and you see this message, then you have a problem, " +
                    "as the backup did not transitioned while you had shutdown the app");
            checkSet.add(Application.class.getSimpleName());
        } else {
            logger.info("The application is successfully rolled");
        }
    }


//    @Factory
//    @Replaces(ObjectMapperFactory.class)
//    static class CustomObjectMapperFactory extends ObjectMapperFactory {
//
//        @Override
//        @Singleton
//        @Replaces(ObjectMapper.class)
//        public ObjectMapper objectMapper(JacksonConfiguration jacksonConfiguration, JsonFactory jsonFactory) {
//            final ObjectMapper mapper = super.objectMapper(jacksonConfiguration, jsonFactory);
//            return mapper;
//        }
//    }
}