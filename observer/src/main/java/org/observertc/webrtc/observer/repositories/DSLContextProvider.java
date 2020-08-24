package org.observertc.webrtc.observer.repositories;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.RecordMapperProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class for creating contexts for jooq used in all repositories
 */
@Singleton
public class DSLContextProvider implements IDSLContextProvider {
	private static Logger logger = LoggerFactory.getLogger(DSLContextProvider.class);

	private static HikariDataSource makeDataSource(HikariConfiguration hikariConfiguration) {
		HikariConfig jdbcConfig = new HikariConfig();
		jdbcConfig.setPoolName(hikariConfiguration.poolName);
		jdbcConfig.setMaximumPoolSize(hikariConfiguration.maxPoolSize);
		jdbcConfig.setMinimumIdle(hikariConfiguration.minIdle);
		jdbcConfig.setJdbcUrl(hikariConfiguration.jdbcURL);
		jdbcConfig.setUsername(hikariConfiguration.username);
		jdbcConfig.setPassword(hikariConfiguration.password);
		jdbcConfig.setDriverClassName(hikariConfiguration.jdbcDriver);
		// Add HealthCheck
//		jdbcConfig.setHealthCheckRegistry(healthCheckRegistry);
		// Add Metrics
//		jdbcConfig.setMetricRegistry(metricRegistry);
		Exception exception = null;
		for (int tried = 0; tried < 3; ++tried) {
			try {
				HikariDataSource result = new HikariDataSource(jdbcConfig);
				return result;
			} catch (Exception ex) {
				logger.warn("An exception happened while trying to connect to the database. This is the {} try. ", tried);
				exception = ex;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				continue;
			}
		}
		throw new RuntimeException(exception);
	}

	private static Configuration getJooqConfiguration(DataSource dataSource, JooqConfiguration jooqConfiguration,
													  RecordMapperProvider recordMapperProvider) {
		DefaultConfiguration result = new DefaultConfiguration();
		switch (jooqConfiguration.dialect.toLowerCase()) {
			case "mysql":
				result.setSQLDialect(SQLDialect.MYSQL);
			default:
				new RuntimeException("Unsupported dialect: " + jooqConfiguration.dialect);
		}

		result.setDataSource(dataSource);
		result.set(recordMapperProvider);
		return result;
	}

	private final Configuration configuration;

	public DSLContextProvider(HikariConfiguration hikariConfiguration, JooqConfiguration jooqConfiguration,
							  RecordMapperProvider recordMapperProvider) {
		DataSource dataSource = makeDataSource(hikariConfiguration);
		this.configuration = getJooqConfiguration(dataSource, jooqConfiguration, recordMapperProvider);
	}

	@Override
	public DSLContext get() {
		return DSL.using(this.configuration);
	}

}
