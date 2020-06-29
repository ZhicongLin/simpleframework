package org.simpleframework.context;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simpleframework.context.annotation.BeanScan;
import org.simpleframework.context.annotation.ClassPathBeanScanner;
import org.simpleframework.context.annotation.SimpleContext;
import org.simpleframework.context.proxy.BeanProxyCreator;
import org.simpleframework.context.proxy.InitializedBean;
import org.simpleframework.context.proxy.annotation.AopListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Description: 启动方法
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
@Slf4j
public class SimpleContainerLoader {

    public static void run(Class<?> clazz) throws Exception {
        //初始化bean
        createBean(clazz);
        //注入属性
        injectBean();
        //执行初始化成功后的方法
        initalized();
    }

    private static void createBean(Class<?> clazz) {
        final ClassPathBeanScanner beanScanner = new ClassPathBeanScanner(clazz);
        final BeanScan beanScan = clazz.getDeclaredAnnotation(BeanScan.class);
        if (beanScan != null) {
            beanScanner.addPackage(beanScan.value());
        }
        final Set<Class<?>> beanClasses = beanScanner.getBeanClasses();
        final List<Class<?>> others = new ArrayList<>();
        for (Class<?> beanClass : beanClasses) {
            final AopListener annotation = beanClass.getAnnotation(AopListener.class);
            if (annotation == null) {
                others.add(beanClass);
                continue;
            }
            BeanProxyCreator.build(beanClass);
            SimpleContextHolder.addAopListenerClass(annotation, beanClass);
        }
        for (Class<?> beanClass : others) {
            BeanProxyCreator.build(beanClass);
        }
    }

    /**
     * 属性注入javaBean
     * @throws ClassNotFoundException
     */
    private static void injectBean() throws ClassNotFoundException {
        final SimpleContext context = SimpleContextHolder.getContext();
        final Set<String> keySet = context.keySet();
        for (String className : keySet) {
            final Object bean = context.get(className);
            final Field[] fields = Class.forName(className).getDeclaredFields();
            for (Field fd : fields) {
                try {
                    fd.setAccessible(true);
                    final Object o = fd.get(bean);
                    if (o != null) {
                        continue;
                    }
                    final Class<?> declaringClass = fd.getType();
                    final Object object = SimpleContextHolder.getBean(declaringClass);
                    if (object == null) {
                        continue;
                    }
                    fd.set(bean, object);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    //调用执行初始化的方法
    private static void initalized() {
        final List<InitializedBean> initBeans = SimpleContextHolder.getInitBeans();
        for (InitializedBean initBean : initBeans) {
            initBean.runner();
        }
    }

}