package org.observertc.webrtc.common.reportsink;

import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.builders.AbstractBuilder;
import org.observertc.webrtc.common.builders.IReportServiceBuilder;
import org.observertc.webrtc.common.reports.Report;

public class ReportServiceBuilder extends ReportServiceAbstractBuilder {

	public ReportServiceBuilder() {

	}

	@Override
	public Processor<UUID, Report> build() {
		Map<String, Object> configuration = this.buildConfigurations();
		Config config = this.convertAndValidate(Config.class, configuration);
		IReportServiceBuilder builder = this.getBuilder(config.builder);
		if (config.configuration == null) {
			return builder.build();
		}
		return builder
				.withConfiguration(config.configuration)
				.build();
	}

	/**
	 * Gets a Builder class implements the {@link IReportServiceBuilder} for the class name
	 *
	 * @param builderType the name of the class.
	 * @return
	 */
	private IReportServiceBuilder getBuilder(String builderType) {
		IReportServiceBuilder result;
		if (builderType.contains(".") == false) {
			result = this.invoke("org.observertc.webrtc.service.reportsink" + builderType);
		} else {
			result = this.invoke(builderType);
		}
		return result
				.withProfiles(this.getProfiles());
	}

	public static class Config extends AbstractBuilder.Config {

	}

}

