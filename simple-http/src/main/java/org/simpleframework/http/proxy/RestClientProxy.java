package org.simpleframework.http.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.simpleframework.http.HttpExecutor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
 * 2020/6/17.1    linzc       2020/6/17     Create
 * </pre>
 * @date 2020/6/17
 */
@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class RestClientProxy implements MethodInterceptor {

    private Object object;

    private final Enhancer enhancer = new Enhancer();

    /**
     * 创建代理对象
     *
     * @param clazz
     */
    public RestClientProxy(Class<?> clazz) {
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        object = enhancer.create();
    }

    /**
     * 获取代理对象
     *
     * @return
     */
    public Object getObject() {
        return object;
    }

    /**
     * 方法代理拦截
     *
     * @param obj
     * @param method
     * @param arguments
     * @param methodProxy
     * @return
     * @throws Throwable
     */
    public Object intercept(Object obj, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
        final RestObject restObject = RestObjectBuilder.create(method, arguments).build();
        if (restObject == null) {
            try {
                return methodProxy.invokeSuper(obj, arguments);
            } catch (NoSuchMethodError e) {
                log.error("NoSuchAnnotation [@Rest] of Method '{}.{}(..)'", method.getDeclaringClass(), method.getName(), e);
                return null;
            }
        } else {
            return HttpExecutor.execute(restObject);
        }
    }

}