package org.observertc.observer.repositories.tasks;

import org.observertc.observer.common.Task;
import org.observertc.schemas.reports.CallEventReport;

import java.util.List;

public interface CallEventReportTask<T> extends Task<List<CallEventReport>> {
    CallEventReportTask<T> withDTO(T DTO);
    CallEventReportTask<T> withDTOAndTimestamp(T DTO, Long timestamp);
}
