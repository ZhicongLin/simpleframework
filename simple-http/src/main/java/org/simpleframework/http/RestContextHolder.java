package org.simpleframework.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.http.proxy.RestObject;

import lombok.extern.slf4j.Slf4j;

/**
 * Description: 上下文管理器
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
    // Rest Client Context Holder
    private static final Map<String, Object> restClientContext = new HashMap<>();

    // Method Rest Object Context Holder
    private static final Map<String, RestObject> restObjectContext = new HashMap<>();

    /**
     * 添加请求方法@RestMapping的RestObject对象
     *
     * @param restObject
     */
    public static void addRestObject(RestObject restObject) {
        final String key = getRestObjectKey(restObject.getMethod(), restObject.getClazz());
        final RestObject ro = restObjectContext.get(key);
        if (ro != null) {
            return;
        }
        restObjectContext.put(key, restObject);
        log.debug("Creating shared instance of singleton method '{}'", key);
    }

    /**
     * 添加注解@RestClient的bean
     *
     * @param restClass
     * @param bean
     * @return
     */
    public static void addBean(Class<?> restClass, Object bean) {
        final String key = restClass.getName();
        Object ro = restClientContext.get(key);
        if (ro != null) {
            return;
        }
        restClientContext.put(key, bean);
    }

    /**
     * 获取注解@RestClient的bean
     *
     * @param restClass
     * @param <T>
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T getBean(Class<T> restClass) {
        return (T) restClientContext.get(restClass.getName());
    }

    /**
     * 获取restObject对象
     *
     * @param method
     * @return
     */
    public static RestObject getRestBean(Method method) {
        final String restObjectKey = getRestObjectKey(method, method.getDeclaringClass());
        return restObjectContext.get(restObjectKey);
    }


    private static String getRestObjectKey(Method method, Class<?> clazz) {
        return clazz.getName() + "." + method.getName();
    }

    /**
     * 获取@RestClient的Map
     *
     * @return
     */
    public static Map<String, Object> getContext() {
        return restClientContext;
    }

}