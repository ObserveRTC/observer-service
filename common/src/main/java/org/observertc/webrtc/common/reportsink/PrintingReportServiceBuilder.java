//package org.observertc.webrtc.common.reportsink;
//
//import org.observertc.webrtc.common.builders.AbstractBuilder;
//
//public class PrintingReportServiceBuilder extends ReportServiceAbstractBuilder {
//
//	public ReportService build() {
//		Config config = this.convertAndValidate(Config.class);
//		PrintingReportService result = new PrintingReportService(config.useLogger);
//		return result;
//	}
//
//	public static class Config extends AbstractBuilder.Config {
//		public boolean useLogger = false;
//	}
//}
