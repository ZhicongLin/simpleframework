package org.simpleframework.context;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simpleframework.context.annotation.SimpleContext;
import org.simpleframework.context.proxy.InitializedBean;
import org.simpleframework.context.proxy.annotation.AopListener;

import ch.qos.logback.core.pattern.color.ANSIConstants;

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
public class SimpleContextHolder {
    private static final Map<Class<? extends Annotation>, Class<?>> aopAnnotations = new HashMap<>();
    private static final SimpleContext context = new SimpleContext();
    private static final List<InitializedBean> initBeans = new ArrayList<>();
    public static void put(String beanName, Object bean) {
        context.put(beanName, bean);
        if (bean instanceof InitializedBean) {
            initBeans.add((InitializedBean)bean);
        }
    }

    public static <T extends Object> T getBean(Class<T> clazz) {
        final Object object = context.get(clazz.getName());
        if (object != null) {
            return (T) object;
        }
        return null;
    }

    public static <T extends Object> T getBean(String name) {
        final Set<String> keySet = context.keySet();
        final String suffix = "." + name;
        for (String key : keySet) {
            if (key.endsWith(suffix)) {
                return (T) context.get(key);
            }
        }
        return null;
    }

    public static Set<Class<? extends Annotation>> getAopAnnotations() {
        return aopAnnotations.keySet();
    }

    public static Class<?> getAopListener(Class<? extends Annotation> annotation) {
        return aopAnnotations.get(annotation);
    }

    public static void addAopListenerClass(AopListener aopListener, Class<?> beanClass) {
        final Class<? extends Annotation> value = aopListener.value();
        aopAnnotations.put(value, beanClass);
    }

    public static SimpleContext getContext() {
        return context;
    }

    public static List<InitializedBean> getInitBeans() {
        return initBeans;
    }

    public static Map<Class<?>, Object> getAnnotationContext(Class<? extends Annotation> annotationClazz) {
        final Map<Class<?>, Object> beans = new HashMap<>();
        final Set<Class<?>> keyClass = context.getKeyClass();
        for (Class<?> clazz : keyClass) {
            final Annotation annotation = clazz.getAnnotation(annotationClazz);
            if (annotation != null) {
                beans.put(clazz, getBean(clazz));
            }
        }
        return beans;
    }
}