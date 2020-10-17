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

package org.observertc.webrtc.observer.controllers;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micronaut.configuration.metrics.annotation.RequiresMetrics;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import javax.inject.Inject;

@RequiresMetrics
@Controller("/prometheusMetrics")
public class PrometheusController {

	@Inject
	MeterRegistry meterRegistry;

	private final PrometheusMeterRegistry prometheusMeterRegistry;

	@Inject
	public PrometheusController(PrometheusMeterRegistry prometheusMeterRegistry) {
		this.prometheusMeterRegistry = prometheusMeterRegistry;
	}

	@Get
	@Produces("text/plain")
	public String metrics() {
		return prometheusMeterRegistry.scrape();
	}
}