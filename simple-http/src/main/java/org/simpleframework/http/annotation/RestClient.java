package org.simpleframework.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.simpleframework.http.io.AbstractRestFilter;
import org.simpleframework.http.io.RestFilter;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RestClient {

    String url() default "";

    Class<?> fallback() default Void.class;

    Class<? extends RestFilter> filter() default AbstractRestFilter.class;
}
