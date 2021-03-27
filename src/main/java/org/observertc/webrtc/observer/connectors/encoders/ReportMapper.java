package org.observertc.webrtc.observer.connectors.encoders;

import org.apache.avro.specific.SpecificRecordBase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ReportMapper implements Function<SpecificRecordBase, Map<String, Object>> {
    private List<FieldValueResolver> resolvers = new LinkedList<>();
    private Map<String, ReportMapper> embeddedResolvers = new HashMap<>();

    @Override
    public Map<String, Object> apply(SpecificRecordBase subject) {
        Map<String, Object> result = new HashMap<>();
        for (FieldValueResolver resolver : this.resolvers) {
            FieldValueResolver.Result resolvedValue = resolver.apply(subject);
            result.put(resolvedValue.fieldName, resolvedValue.fieldValue);
        }

        for (Map.Entry<String, ReportMapper> entry : embeddedResolvers.entrySet()) {
            String fieldName = entry.getKey();
            ReportMapper reportMapper = entry.getValue();
            SpecificRecordBase embeddedRecord = (SpecificRecordBase) subject.get(fieldName);
            Map<String, Object> embeddedResult = reportMapper.apply(embeddedRecord);
            result.putAll(embeddedResult);
        }
        return result;
    }

    public<TIn, TOut> ReportMapper add(String fieldName, Function<String, String> fieldAdapter, Function<TIn, TOut> valueAdapter) {
        var resolver = new FieldValueResolver(fieldName, fieldAdapter, valueAdapter);
        this.resolvers.add(resolver);
        return this;
    }

    public ReportMapper add(String fieldName, ReportMapper reportMapper) {
        this.embeddedResolvers.put(fieldName, reportMapper);
        return this;
    }

}
