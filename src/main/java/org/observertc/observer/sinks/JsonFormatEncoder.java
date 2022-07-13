package org.observertc.observer.sinks;

import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class JsonFormatEncoder<K, V> implements FormatEncoder<K, V> {

    private static final Logger defaultLogger = LoggerFactory.getLogger(JsonFormatEncoder.class);

    private final Function<ReportType, K> typeMapper;
    private final Function<Report, V> formatMapper;
    private final Logger logger;

    public JsonFormatEncoder(Function<ReportType, K> typeMapper, Function<Report, V> formatMapper, Logger logger) {
        this.typeMapper = typeMapper;
        this.formatMapper = formatMapper;
        this.logger = logger;
    }

    @Override
    public Map<K, List<V>> map(List<Report> reports) {
        try {
            Map<K, List<V>> records = new HashMap<>();
            for (var report : reports) {
                V myRecord = this.formatMapper.apply(report);
                if (myRecord == null) {
                    logger.warn("Cannot map report {}", JsonUtils.objectToString(report));
                    continue;
                }

                var type = this.typeMapper.apply(report.type);
                if (type == null) {
                    continue;
                }
                var deliveryRecords = records.get(type);
                if (deliveryRecords == null) {
                    deliveryRecords = new LinkedList<>();
                    records.put(type, deliveryRecords);
                }
                deliveryRecords.add(myRecord);
            }
            return records;
        } catch (Exception e) {
            this.logger.warn("Exception occuirred while transforming", e);
            return null;
        }
    }

}
