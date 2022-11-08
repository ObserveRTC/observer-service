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

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@Singleton
@Requires(notEnv = Environment.TEST)
public class ServiceStartedListener implements ApplicationEventListener<ServerStartupEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceStartedListener.class);

	@Inject
	HamokService hamokService;

	@Inject
	ObserverConfig observerConfig;

	@Inject
	ObserverService observerService;

	@Inject
	BackgroundTasksExecutor backgroundTasksExecutor;

	private Properties properties;

	@PostConstruct
	void setup() {
		if (observerConfig.security.printConfigs) {
			logger.info("Config {}", JsonUtils.objectToString(observerConfig));
		}
		if (this.backgroundTasksExecutor.isStarted()) {
			this.backgroundTasksExecutor.stop();
		}
		this.properties = this.fetch();
	}

	@PreDestroy
	void teardown() {
		if (observerService.isStarted()) {
			observerService.stop();
		}
		if (this.hamokService.isRunning()) {
			hamokService.stop();
		}
	}

	public ServiceStartedListener() {
		
	}


	@Override
	public void onApplicationEvent(ServerStartupEvent event) {
		observerService.start();
		renderLogoAndVersion();
		hamokService.start();
		// TODO: websocket status message
		// TODO: rest api status page
		// TODO: sinks status
	}

	private void renderLogoAndVersion() {
		URL logoUri = ClassLoader.getSystemResource("logo.txt");
		try {
			var path = Path.of(logoUri.getPath());
			List<String> lines = Files.readAllLines(path);
			var text = String.join("\n", lines);
			System.out.println(text);
			System.out.println("Observer version: " + this.properties.getProperty("version"));
		} catch (Throwable t) {
			logger.warn("Error rendering logo", t);
		}

	}

	private Properties fetch() {
		URL serviceUri = ClassLoader.getSystemResource("service.properties");
		try {
			var path = Path.of(serviceUri.getPath());
			FileInputStream inputStream = new FileInputStream(path.toFile());
			var properties = new Properties();
			properties.load(inputStream);
			return properties;
		} catch (Throwable t) {
			logger.warn("Error reading service.properties", t);
			return new Properties();
		}
	}
}
