package org.observertc.webrtc.observer.monitors;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Around
@Type(CounterInterceptor.class)
@Target(ElementType.METHOD)
public @interface CountInvocations {
    String value() default "";
}


