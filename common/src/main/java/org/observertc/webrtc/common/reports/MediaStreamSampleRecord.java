package org.observertc.webrtc.common.reports;

public interface MediaStreamSampleRecord {

	Long getMinimum();

	Long getMaximum();

	Long getPresented();

	Long getEmpty();

	Long getSum();
}
