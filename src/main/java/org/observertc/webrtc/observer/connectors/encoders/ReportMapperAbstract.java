package org.observertc.webrtc.observer.connectors.encoders;

import org.apache.avro.specific.SpecificRecordBase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReportMapperAbstract<T> implements Function<SpecificRecordBase, T> {
    private Supplier<T> supplier;
    private Map<String, BiConsumer<T, Object>> mappers = new HashMap<>();
    private Map<String, ReportMapperAbstract<T>> embeddedResolvers = new HashMap<>();

    @Override
    public T apply(SpecificRecordBase subject) {
        T result = this.supplier.get();
        return this.apply(result, subject);
    }

    private T apply(T result, SpecificRecordBase subject) {
        for (Map.Entry<String, BiConsumer<T, Object>> mapperEntry : this.mappers.entrySet()) {
            String fieldName = mapperEntry.getKey();
            BiConsumer<T, Object> mapper = mapperEntry.getValue();
            Object value = subject.get(fieldName);
            mapper.accept(result, value);
        }

        for (Map.Entry<String, ReportMapperAbstract<T>> entry : embeddedResolvers.entrySet()) {
            String fieldName = entry.getKey();
            ReportMapperAbstract reportMapper = entry.getValue();
            SpecificRecordBase embeddedRecord = (SpecificRecordBase) subject.get(fieldName);
            reportMapper.apply(result, embeddedRecord);
        }
        return result;
    }

    public ReportMapperAbstract<T> add(String fieldName, BiConsumer<T, Object> mapper) {
        this.mappers.put(fieldName, mapper);
        return this;
    }

    public ReportMapperAbstract add(String fieldName, ReportMapperAbstract reportMapper) {
        this.embeddedResolvers.put(fieldName, reportMapper);
        return this;
    }

    public ReportMapperAbstract<T> withSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }
}
