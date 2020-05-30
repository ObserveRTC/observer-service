package com.observertc.gatekeeper.webrtcstat.repositories;

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

/**
 * This is a helper class for creating contexts for jooq used in all repositories
 */
@Singleton
public class DSLContextProvider implements IDSLContextProvider {

	public static HikariDataSource makeDataSource(HikariConfiguration hikariConfiguration) {
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
		return new HikariDataSource(jdbcConfig);
	}

	private final Configuration configuration;

	public DSLContextProvider(HikariConfiguration hikariConfiguration, RecordMapperProvider recordMapperProvider) {
		DataSource dataSource = makeDataSource(hikariConfiguration);
		DefaultConfiguration configuration = new DefaultConfiguration();
		configuration.setSQLDialect(SQLDialect.MYSQL);
		configuration.setDataSource(dataSource);
		configuration.set(recordMapperProvider);
		this.configuration = configuration;
	}

	@Override
	public DSLContext get() {
		return DSL.using(this.configuration);
	}
}
