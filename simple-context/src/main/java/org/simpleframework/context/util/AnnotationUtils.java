package org.simpleframework.context.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.simpleframework.context.annotation.Bean;

/**
 * Description:
 *
 * @author linzc
 * @version 1.0
 *
 * <pre>
 * 修改记录:
 * 修改后版本        修改人     修改日期        修改内容
 * 2020/6/15.1    linzc       2020/6/15     Create
 * </pre>
 * @date 2020/6/15
 */
public class AnnotationUtils {

    private static final List<String> ANN = new ArrayList<>();
    static {
        ANN.add(Documented.class.getName());
        ANN.add(Target.class.getName());
        ANN.add(Retention.class.getName());
    }

    public static boolean isBeanClass(Class<?> clazz) {
        final Bean beanAnn = clazz.getAnnotation(Bean.class);
        if (beanAnn != null) {
            return true;
        }
        if (ANN.contains(clazz.getName())) {
            return false;
        }
        final Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            final Class<? extends Annotation> annotationClass = annotation.annotationType();
            final boolean beanClass = isBeanClass(annotationClass);
            if (beanClass) {
                return true;
            }
        }
        return false;
    }
}