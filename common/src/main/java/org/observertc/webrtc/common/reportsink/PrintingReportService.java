package org.observertc.webrtc.common.reportsink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintingReportService implements ReportService {

	private static final Logger logger = LoggerFactory.getLogger(PrintingReportService.class);

	private ObjectMapper mapper = new ObjectMapper();

	public PrintingReportService() {

	}

	@Override
	public void init(ProcessorContext context) {

	}

	@Override
	public void process(UUID key, Report report) {
		if (report == null) {
			logger.warn("The provided report for {} is null", key);
			return;
		}
		String json;
		try {
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			json = "Not parsable report";
		}
		System.out.println(json);
	}

	@Override
	public void close() {

	}
}
