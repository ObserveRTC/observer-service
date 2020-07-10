package org.observertc.webrtc.common.reportsink;

import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.builders.AbstractBuilder;
import org.observertc.webrtc.common.builders.IConfigurationProfiles;
import org.observertc.webrtc.common.builders.IReportServiceBuilder;
import org.observertc.webrtc.common.reports.Report;

public abstract class ReportServiceAbstractBuilder extends AbstractBuilder implements IReportServiceBuilder {

	public ReportServiceAbstractBuilder() {

	}

	public ReportServiceAbstractBuilder withProfiles(IConfigurationProfiles profiles) {
		this.setProfiles(profiles);
		return this;
	}

	public ReportServiceAbstractBuilder withConfiguration(Map<String, Object> configs) {
		this.getConfigs().putAll(configs);
		return this;
	}

	public ReportServiceAbstractBuilder withConfiguration(String key, Object value) {
		this.getConfigs().put(key, value);
		return this;
	}

	public abstract Processor<UUID, Report> build();

	public static class Config extends AbstractBuilder.Config {

	}

}

