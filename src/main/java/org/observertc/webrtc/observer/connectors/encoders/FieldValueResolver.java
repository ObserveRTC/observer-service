package org.observertc.webrtc.observer.connectors.encoders;

import org.apache.avro.specific.SpecificRecordBase;

import java.util.function.Function;

public class FieldValueResolver implements Function<SpecificRecordBase, FieldValueResolver.Result> {
    private final String fieldName;
    private final Function valueResolver;
    private final Function<String, String> fieldNameResolver;

    public FieldValueResolver(String fieldName, Function valueMapper) {
        this(fieldName, Function.identity(), valueMapper);
    }

    public FieldValueResolver(String fieldName, Function<String, String> fieldNameResolver, Function valueResolver) {
        this.fieldName = fieldName;
        this.fieldNameResolver = fieldNameResolver;
        this.valueResolver = valueResolver;
    }

    public FieldValueResolver.Result apply(SpecificRecordBase record) {
        Object fieldValue = record.get(this.fieldName);
        String newField = this.fieldNameResolver.apply(this.fieldName);
        Object newValue = this.valueResolver.apply(fieldValue);
        var result = new Result(newField, newValue);
        return result;
    }

    static class Result {
        final String fieldName;
        final Object fieldValue;

        Result(String fieldName, Object fieldValue) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
        }
    }

}
