package io.gridgo.boot.support.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(value = FIELD)
@Retention(value = RUNTIME)
public @interface ComponentInject {

    public String value() default "";
}
