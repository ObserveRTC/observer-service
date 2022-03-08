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
import io.micronaut.discovery.event.ServiceStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class ServiceShutdownListener implements ApplicationEventListener<ServiceStoppedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceShutdownListener.class);

	@Inject
	ObserverService observerService;

	@Inject
	ObserverHazelcast observerHazelcast;

	public ServiceShutdownListener() {
		
	}


	private void init() {

	}

	@Override
	public void onApplicationEvent(ServiceStoppedEvent event) {
		logger.info("Shutdown started");
		observerService.stop();
//		this.observerHazelcast.getInstance().getConfig().getMapConfig("whatever").getMapStoreConfig().setEnabled(false);
		this.observerHazelcast.getInstance().getLifecycleService().shutdown();
		logger.info("Shutdown ended");
	}
}
