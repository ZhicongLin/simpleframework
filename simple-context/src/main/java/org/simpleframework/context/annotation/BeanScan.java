package org.simpleframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface BeanScan {

    /**
     * 如果为空，则默认扫描注解当前注解的类所在的包路径下
     */
    String[] value() default "org.simpleframework.context";
}
