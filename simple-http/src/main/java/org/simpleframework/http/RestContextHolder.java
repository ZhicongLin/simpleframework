package org.simpleframework.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simpleframework.http.proxy.RestObject;

import lombok.extern.slf4j.Slf4j;

/**
 * Description: Http工具管理类
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
@Slf4j
public class RestContextHolder {
    // Rest Context Holder
    private static final Map<String, Object> restContext = new HashMap<>();

    private static final Map<String, RestObject> restObjectContext = new HashMap<>();

    public static void addRestObject(RestObject restObject) {
        final String key = getRestObjectKey(restObject.getMethod(), restObject.getClazz());
        final RestObject ro = restObjectContext.get(key);
        if (ro != null) {
            return;
        }
        restObjectContext.put(key, restObject);
        log.info("Creating shared instance of singleton method '{}'", key);
    }

    public static <T> T getBean(Class<T> restClass) {
        return (T) restContext.get(restClass.getName());
    }

    public static RestObject getRestBean(Method method) {
        final String restObjectKey = getRestObjectKey(method, method.getDeclaringClass());
        return restObjectContext.get(restObjectKey);
    }

    public static void addBean(Class<?> restClass, Object bean) {
        final String key = restClass.getName();
        Object ro = restContext.get(key);
        if (ro != null) {
            return;
        }
        restContext.put(key, bean);
    }

    private static String getRestObjectKey(Method method, Class<?> clazz) {
        return clazz.getName() + "." + method.getName();
    }

    public static Map<String, Object> getContext() {
        return restContext;
    }

}