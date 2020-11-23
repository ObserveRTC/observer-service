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

import java.io.File;
import java.io.IOException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockFileDatabase;
import org.observertc.webrtc.observer.repositories.IDSLContextProvider;
import org.observertc.webrtc.observer.repositories.mappers.RecordMapperProviderImpl;

public class MockedDSLContextProvider implements IDSLContextProvider {

	private final MockConnection connection;

	public MockedDSLContextProvider(String resourceFileName) {
		String path = getClass().getClassLoader().getResource(resourceFileName).getFile();
		this.connection = this.connectFileDatabase(new File(path));
	}

	@Override
	public DSLContext get() {
		DefaultConfiguration configuration = new DefaultConfiguration();
		configuration.setSQLDialect(SQLDialect.MYSQL);
		configuration.setConnection(this.connection);
		configuration.set(new RecordMapperProviderImpl());
		return DSL.using(configuration);
	}

	private MockConnection connectFileDatabase(File file) {
		MockDataProvider provider = null;
		try {
			provider = new MockFileDatabase(file);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new MockConnection(provider);
	}
}
