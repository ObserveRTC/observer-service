package org.observertc.webrtc.observer.service.gatekeeper;

import org.observertc.webrtc.observer.service.repositories.IDSLContextProvider;
import org.observertc.webrtc.observer.service.repositories.mappers.RecordMapperProviderImpl;
import java.io.File;
import java.io.IOException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockFileDatabase;

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
