package org.observertc.observer.configbuilders;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, TYPE })
@Retention(RUNTIME)
@Documented
public @interface ConfigAssent {
    boolean mutable() default true;

    String keyField() default "";
}
