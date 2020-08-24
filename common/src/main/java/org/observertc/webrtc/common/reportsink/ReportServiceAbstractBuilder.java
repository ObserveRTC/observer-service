//package org.observertc.webrtc.common.reportsink;
//
//import java.util.Map;
//import org.observertc.webrtc.common.builders.AbstractBuilder;
//import org.observertc.webrtc.common.builders.IConfigurationProfiles;
//import org.observertc.webrtc.common.builders.IReportServiceBuilder;
//
//public abstract class ReportServiceAbstractBuilder extends AbstractBuilder implements IReportServiceBuilder {
//
//	public ReportServiceAbstractBuilder() {
//
//	}
//
//	public ReportServiceAbstractBuilder withProfiles(IConfigurationProfiles profiles) {
//		this.setProfiles(profiles);
//		return this;
//	}
//
//	public ReportServiceAbstractBuilder withConfiguration(Map<String, Object> configs) {
//		this.getConfigs().putAll(configs);
//		return this;
//	}
//
//	public ReportServiceAbstractBuilder withConfiguration(String key, Object value) {
//		this.getConfigs().put(key, value);
//		return this;
//	}
//
//	public abstract ReportService build();
//
//	public static class Config extends AbstractBuilder.Config {
//
//	}
//
//}
//
