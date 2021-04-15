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
import io.micrometer.core.instrument.util.StringUtils;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.observertc.webrtc.observer.common.Sleeper;
import org.observertc.webrtc.observer.configbuilders.ObservableConfig;
import org.observertc.webrtc.observer.evaluators.Pipeline;
import org.observertc.webrtc.observer.configs.stores.ServiceMapsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


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
    private static final String CONNECTOR_CONFIG_FILES_SYSTEM_ENV = "CONNECTOR_CONFIG_FILES";
    public static ApplicationContext context;

    public static void main(String[] args) {
        Sleeper.makeFromSystemEnv(INITIAL_WAIT_IN_S, ChronoUnit.SECONDS).run();
        context = Micronaut.run(Application.class);
        // To make the pipeline run!
        Pipeline pipeline = context.getBean(Pipeline.class);
        ObserverHazelcast observerHazelcast = context.getBean(ObserverHazelcast.class);
        logger.info("Hazelcast configuration: {}", observerHazelcast.toString());
        deployCheck(observerHazelcast);
        logger.info("ServicesRepository config");
        context.getBean(ServiceMapsStore.class);
        loadConnectorConfigFiles();
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

    private static void loadConnectorConfigFiles() {
        List<String> paths = new LinkedList<>();
        String connectorConfigFiles = System.getenv(CONNECTOR_CONFIG_FILES_SYSTEM_ENV);
        if (Objects.isNull(connectorConfigFiles)) {
            return;
        }

        logger.info("Loading files {}", connectorConfigFiles);
        Arrays.asList(connectorConfigFiles.split(",")).stream().forEach(paths::add);

        AtomicReference<Throwable> error = new AtomicReference<>(null);
        Connectors connectors = context.getBean(Connectors.class);
        ObservableConfig observerConfig = context.getBean(ObservableConfig.class);
        for (String configPath : paths) {
            if (StringUtils.isBlank(configPath)) {
                continue;
            }
            InputStream inputStream = null;
            try {
                if (configPath.startsWith("classpath:")) {
                    configPath = configPath.substring(10);
                    inputStream = Application.class.getClassLoader()
                            .getResourceAsStream(configPath);
                } else {
                    inputStream = new FileInputStream(configPath);
                }
                if (Objects.isNull(inputStream)) {
                    logger.warn("Cannot find {}", configPath);
                    continue;
                }
                observerConfig
                        .fromYamlInputStream(inputStream)
                        .subscribe(connectors::add, error::set);
                if (Objects.nonNull(error.get())) {
                    logger.error("During connector loading an error happened", error.get());
                    return;
                }
            } catch (Exception e) {
                logger.error("Error during connector configuration loading", e);
                continue;
            }

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