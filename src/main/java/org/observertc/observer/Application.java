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

package org.observertc.observer;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.runtime.Micronaut;
import org.observertc.observer.common.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;


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
//@EnableServiceMonitor(port = "http", path = "/prometheus")
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String INITIAL_WAIT_IN_S = "INITIAL_WAITING_TIME_IN_S";
    private static final String MICRONAUT_CONFIG_FILES = "MICRONAUT_CONFIG_FILES";
    private static final String OBSERVER_CONFIG_FILE = "OBSERVER_CONFIG_FILE";

    public static ApplicationContext context;
    public static void main(String[] args) {
        Sleeper.makeFromSystemEnv(INITIAL_WAIT_IN_S, ChronoUnit.SECONDS).run();
        logger.info("Micronaut config file: {}", System.getenv(MICRONAUT_CONFIG_FILES));
        var observerConfig = loadCustomObserverConfig();
        context = Micronaut.build(args)
                .banner(false)
                .properties(observerConfig)
                .include()
                .start();
//        context = Micronaut.run(Application.class, args);

    }

    private static Map<String, Object> loadCustomObserverConfig() {
        String path = System.getenv(OBSERVER_CONFIG_FILE);
        logger.info("{} env variable is {}", OBSERVER_CONFIG_FILE, path);
        if (path == null) {
            return Collections.EMPTY_MAP;
        }
        File file = new File(path);
        if(!file.exists() || file.isDirectory()) {
            logger.warn("Given path {} for {} does not exists or it is a directory", path, OBSERVER_CONFIG_FILE);
            return Collections.EMPTY_MAP;
        }
        try {
            var inputStream = new FileInputStream(path);
            Yaml yaml = new Yaml();
            return yaml.load(inputStream);
        } catch (Exception ex) {
            logger.warn("Exception occurred while reading {} for {} ", path, OBSERVER_CONFIG_FILE, ex);
            return Collections.EMPTY_MAP;
        }
    }
}