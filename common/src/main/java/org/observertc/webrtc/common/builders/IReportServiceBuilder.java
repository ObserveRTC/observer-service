package org.observertc.webrtc.common.builders;

import java.util.UUID;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.reports.Report;

/**
 * An interface for any kind of Builder class
 */
public interface IReportServiceBuilder extends IBuilderAbstract<IReportServiceBuilder> {

	Processor<UUID, Report> build();
}
