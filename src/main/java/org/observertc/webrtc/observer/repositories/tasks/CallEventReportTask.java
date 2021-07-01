package org.observertc.webrtc.observer.repositories.tasks;

import org.observertc.webrtc.observer.common.Task;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import java.util.List;

public interface CallEventReportTask<T> extends Task<List<CallEventReport>> {
    CallEventReportTask<T> withDTO(T DTO);
    CallEventReportTask<T> withDTOAndTimestamp(T DTO, Long timestamp);
}
