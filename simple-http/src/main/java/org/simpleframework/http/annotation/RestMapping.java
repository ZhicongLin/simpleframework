package org.simpleframework.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.simpleframework.http.io.AbstractRestFilter;
import org.simpleframework.http.io.RestFilter;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RestMapping {
    String url() default "";

    RestMethod method() default RestMethod.GET;
    // 添加拦截器
    Class<? extends RestFilter> filter() default AbstractRestFilter.class;
    // 忽略拦截器
    boolean ignoreFilter() default false;
}
