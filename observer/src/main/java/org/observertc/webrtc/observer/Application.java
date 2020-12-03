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

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.runtime.Micronaut;
import java.time.temporal.ChronoUnit;
import org.observertc.webrtc.common.Sleeper;


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
public class Application {
	private static final String INITIAL_WAIT_IN_S = "INITIAL_WAITING_TIME_IN_S";

	public static ApplicationContext context;

	public static void main(String[] args) {
		Sleeper.makeFromSystemEnv(INITIAL_WAIT_IN_S, ChronoUnit.SECONDS).run();
		context = Micronaut.run(Application.class);
	}

}