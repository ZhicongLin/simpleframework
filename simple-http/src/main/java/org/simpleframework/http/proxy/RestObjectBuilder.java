package org.simpleframework.http.proxy;

import java.lang.reflect.Method;

import org.simpleframework.http.RestContextHolder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Description: rest object 请求构建器
 *
 * @author linzc
 * @version 1.0
 *
 * <pre>
 * 修改记录:
 * 修改后版本        修改人     修改日期        修改内容
 * 2020/6/18.1    linzc       2020/6/18     Create
 * </pre>
 * @date 2020/6/18
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestObjectBuilder {

    private Method method;
    private Object[] arguments;


    /**
     * 创建构建器
     * @param method
     * @param arguments
     * @return
     */
    public static RestObjectBuilder create(Method method, Object... arguments) {
        return new RestObjectBuilder(method, arguments);
    }

    /**
     * 构建请求对象
     * @return
     */
    public RestObject build() {
        RestObject ro = RestContextHolder.getRestBean(this.method);
        if (ro == null) {
            ro = newRestObject(this.method);
        }
        if (ro != null) {
            ro.parameterHandle(this.arguments);
            return ro;
        }
        return null;
    }

    /**
     * 创建一个请求对象
     * @param method
     * @return
     */
    public static RestObject newRestObject(Method method) {
        final RestObject ro = new RestObject(method);
        if (ro.getLogger() == null) {
            return null;
        }
        RestContextHolder.addRestObject(ro);
        return ro;
    }
}