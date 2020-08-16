package org.observertc.webrtc.service.reportsink;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.context.annotation.Value;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.builders.ConfigurationLoader;
import org.observertc.webrtc.common.builders.IConfigurationLoader;
import org.observertc.webrtc.common.builders.IConfigurationProfiles;
import org.observertc.webrtc.common.builders.IReportServiceBuilder;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reportsink.ReportServiceBuilder;
import org.observertc.webrtc.service.Application;

@Singleton
public class ReportServiceProvider {
	private static final String REPORTSINK_CONFIGURATION_SOURCE_KEY = "reportsinks";
	private Processor<UUID, Report> reportService = null;
	private IReportServiceBuilder reportServiceBuilder;

	public ReportServiceProvider(@Value("${reportsink.configFile}") String yamlConfigFile,
								 @Value("${reportsink.profile}") String profileKey) throws JsonProcessingException {

		InputStream configFileStream = Application.class.getResourceAsStream(yamlConfigFile);
		IConfigurationLoader configurationLoader = new ConfigurationLoader().withYaml(configFileStream);
		IConfigurationProfiles configurationProfiles = configurationLoader.getConfigurationSourceFor(REPORTSINK_CONFIGURATION_SOURCE_KEY);
		Map<String, Object> configurations = configurationProfiles.getConfigurationFor(profileKey);
		this.reportServiceBuilder =
				new ReportServiceBuilder().withProfiles(configurationProfiles).withConfiguration(configurations);
	}

	public Processor<UUID, Report> getReportService() {
		if (this.reportService == null) {
			this.reportService = this.reportServiceBuilder.build();
		}
		return this.reportService;
	}
}
