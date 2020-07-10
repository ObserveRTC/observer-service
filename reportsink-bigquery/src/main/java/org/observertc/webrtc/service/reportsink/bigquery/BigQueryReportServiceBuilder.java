package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.UUID;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.builders.AbstractBuilder;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reportsink.ReportServiceAbstractBuilder;

public class BigQueryReportServiceBuilder extends ReportServiceAbstractBuilder {

	public Processor<UUID, Report> build() {
		Config config = this.convertAndValidate(Config.class);
		BigQueryService bigQueryService = new BigQueryService(config.projectName, config.datasetName);
		BigQueryReportService result = new BigQueryReportService(bigQueryService);
		return result;
	}

	public static class Config extends AbstractBuilder.Config {

		public String projectName;

		public String datasetName;
	}
}
