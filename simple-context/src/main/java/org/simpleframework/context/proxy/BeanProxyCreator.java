package org.simpleframework.context.proxy;

import org.simpleframework.context.SimpleContextHolder;

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
public class BeanProxyCreator {

    public static Object build(Class<?> clazz) {
        final Object bean = SimpleContextHolder.getBean(clazz);
        if (bean == null) {
            final Object proxy = BeanProxyFactory.createProxy(clazz);
            SimpleContextHolder.put(clazz.getName(), proxy);
            return proxy;
        }
        return bean;
    }
}