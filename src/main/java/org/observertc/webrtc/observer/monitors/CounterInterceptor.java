package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class CounterInterceptor implements MethodInterceptor<Object,Object> {

    @Inject
    MeterRegistry meterRegistry;

    public Object intercept(MethodInvocationContext<Object, Object> context) {
        var valueHolder = context.getExecutableMethod().getValue(CountInvocations.class);
        if (valueHolder.isPresent()) {
            this.meterRegistry.counter(valueHolder.toString()).increment();
        }
        return context.getExecutableMethod();
    }
}
