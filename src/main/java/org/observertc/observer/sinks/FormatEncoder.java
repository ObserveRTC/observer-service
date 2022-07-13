package org.observertc.observer.sinks;

import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.reports.Report;

import java.util.List;
import java.util.Map;

public interface FormatEncoder<K, V> extends Mapper<List<Report>, Map<K, List<V>>> {
}
