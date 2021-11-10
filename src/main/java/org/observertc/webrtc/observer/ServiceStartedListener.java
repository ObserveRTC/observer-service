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

import com.hazelcast.map.IMap;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class ServiceStartedListener implements ApplicationEventListener<ServiceReadyEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceStartedListener.class);

	@Inject
	ObserverHazelcast observerHazelcast;

	@Inject
	ObserverService observerService;

	private Properties properties;

	@PostConstruct
	void setup() {
		this.properties = this.fetch();
		this.deployCheck();
	}

	@PreDestroy
	void teardown() {
		if (observerService.isStarted()) {
			observerService.stop();
		}
		observerHazelcast.getInstance().getCluster().getMembers().stream().forEach(member -> {
			member.getUuid();
		});
	}

	public ServiceStartedListener() {
		
	}


	@Override
	public void onApplicationEvent(ServiceReadyEvent event) {
		observerService.start();
		renderLogoAndVersion();
	}

	/**
	 * Check if the hazelcast contains a value previously
	 * added to the cluster automatically whenever this application was part.
	 *
	 */
	private void deployCheck() {
		final String KEY = ObserverService.class.getSimpleName() + "STAMP";
		IMap<String, String> configMap = observerHazelcast.getInstance().getMap(KEY);

		if (!configMap.containsKey(KEY)) {
			logger.info("It seems this is the first time the service is deployed. " +
					"If you have done RollingUpdate, and you see this message, then you have a problem, " +
					"as the backup did not transitioned while you had shutdown the app");
		} else {
			String prevStamp = configMap.get(KEY);
			logger.info("The service is successfully rolled up. Previous stamp: {}", prevStamp);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
		Date dt = new Date();
		String humanReadableNow = sdf.format(dt); // formats to 09/23/2009 13:53:28.238
		String stamp = String.format("Version %s, deployed at: %s, cluster member: %s", properties.getOrDefault("version", "unknown").toString(), humanReadableNow, observerHazelcast.getMemberName());
		configMap.set(KEY, stamp);
		logger.info("Deploy stamp ({}) is added", stamp);
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
