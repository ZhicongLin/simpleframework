package org.simpleframework.context.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.simpleframework.context.SimpleContextHolder;
import org.simpleframework.context.proxy.annotation.After;
import org.simpleframework.context.proxy.annotation.AopListener;
import org.simpleframework.context.proxy.annotation.Before;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@Getter
public class BeanProxyFactory implements MethodInterceptor {

    private MethodAdvices methodAdvices;

    private final Enhancer enhancer = new Enhancer();

    private BeanProxyFactory(Class<?> clazz) {
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);

        final Set<Class<? extends Annotation>> aopAnnotations = SimpleContextHolder.getAopAnnotations();
        if (aopAnnotations.isEmpty()) {
            return;
        }
        for (Class<? extends Annotation> annotationType : aopAnnotations) {
            final Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                final Annotation annotation = method.getAnnotation(annotationType);
                if (annotation != null) {
                    // bean 添加拦截器
                    addMethodAdvices(annotationType);
                    break;
                }
            }
        }
    }

    private void addMethodAdvices(Class<? extends Annotation> annotationType) {
        if (this.methodAdvices == null) {
            this.methodAdvices = new MethodAdvices();
        }
        final Class<?> aopListener = SimpleContextHolder.getAopListener(annotationType);
        final Method[] methods = aopListener.getMethods();
        for (Method method : methods) {
            final Before before = method.getAnnotation(Before.class);
            if (before != null) {
                methodAdvices.addBefore(aopListener);
            }
            final After after = method.getAnnotation(After.class);
            if (after != null) {
                methodAdvices.addAfter(aopListener);
            }

        }
    }

    private Object getProxy() {
        return enhancer.create();
    }

    public static Object createProxy(Class<?> clazz) {
        log.debug("Creating shared instance of singleton bean '{}'", clazz.getName());
        final BeanProxyFactory handler = new BeanProxyFactory(clazz);
        return handler.getProxy();
    }

    public void doBefore(SimpleMethodProxy simpleMethodProxy) throws Throwable {
        if (this.methodAdvices == null) {
            return;
        }
        final Method method = simpleMethodProxy.getMethod();
        final Annotation[] annotations = method.getAnnotations();
        if (annotations == null) {
            return;
        }
        final List<Class<?>> aopBefores = this.methodAdvices.getAopBefores();
        for (Class<?> aopBefore : aopBefores) {
            final AopListener aopListener = aopBefore.getAnnotation(AopListener.class);
            final Annotation annotation = method.getAnnotation(aopListener.value());
            if (annotation == null) {
                continue;
            }
            final AopBefore before = (AopBefore) BeanProxyCreator.build(aopBefore);
            before.before(simpleMethodProxy);
        }
    }

    private void doAfter(SimpleMethodProxy simpleMethodProxy, Object result) throws Throwable {
        if (this.methodAdvices == null) {
            return;
        }
        final Method method = simpleMethodProxy.getMethod();
        final Annotation[] annotations = method.getAnnotations();
        if (annotations == null) {
            return;
        }
        final List<Class<?>> aopBefores = this.methodAdvices.getAopAfters();
        for (Class<?> aopBefore : aopBefores) {
            final AopListener aopListener = aopBefore.getAnnotation(AopListener.class);
            final Annotation annotation = method.getAnnotation(aopListener.value());
            if (annotation == null) {
                continue;
            }
            final AopAfter before = (AopAfter) BeanProxyCreator.build(aopBefore);
            before.after(simpleMethodProxy, result);
        }
    }


    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        final SimpleMethodProxy simpleMethodProxy = new SimpleMethodProxy(o, method, objects, methodProxy);
        doBefore(simpleMethodProxy);
        final Object result = methodProxy.invokeSuper(simpleMethodProxy.getBean(), simpleMethodProxy.getArgs());
        doAfter(simpleMethodProxy, result);
        return result;
    }
}