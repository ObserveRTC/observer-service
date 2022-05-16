package org.observertc.observer.sinks.firehose;


import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.sinks.Sink;

@Prototype
public class FirehoseSinkBuilder extends AbstractBuilder implements Builder<Sink> {

    @Override
    public <S extends Sink> S build() {
        return null;
    }
}
