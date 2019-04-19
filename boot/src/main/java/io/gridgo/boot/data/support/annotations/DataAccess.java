package io.gridgo.boot.data.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataAccess {

    String gateway() default "";

    Class<?> gatewayClass() default DEFAULT.class;

    String schema();

    final class DEFAULT {}
}
