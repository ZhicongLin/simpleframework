package org.simpleframework.http.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.annotation.RestClient;
import org.simpleframework.http.annotation.RestMapping;
import org.simpleframework.http.annotation.RestMethod;
import org.simpleframework.http.io.AbstractRestFilter;
import org.simpleframework.http.io.RestFilter;
import org.simpleframework.http.proxy.visitor.MethodParamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * Description: Rest请求对象
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
@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
public class RestObject {
    // 过滤器集合
    private static final Map<String, RestFilter> FILTERS = new HashMap<>();
    // 方法
    private Method method;
    // 方法所属类
    private Class<?> clazz;
    // 返回类型
    private Class<?> returnType;
    // 返回类型的泛型
    private Class<?> genericReturnType;
    // 返回类型是否数组
    private boolean isArray;
    // 请求连接
    private String url;
    // 请求体
    private String body;
    // 请求参数
    private Map<String, Object> params;
    // 请求方式
    private RestMethod restMethod;
    // 头部信息
    private Map<String, String> httpHeaders;
    // 日志类
    private Logger logger;
    // 过滤器类型
    private Class<? extends RestFilter> filterClass;
    // 过滤器
    private RestFilter filter;
    // 是否忽略过滤器
    private boolean ignoreFilter;

    public RestObject(Method method) {
        this.method = method;
        this.clazz = method.getDeclaringClass();
        // 处理注解
        this.annotationHandle();
        // 处理返回类型
        this.returnTypeHandle();
    }

    /**
     * 处理返回类型
     */
    @SneakyThrows
    private void returnTypeHandle() {
        this.returnType = this.method.getReturnType();
        if (this.returnType.isArray()) {
            this.isArray = true;
            final String typeName = this.method.getGenericReturnType().getTypeName();
            this.genericReturnType = ClassUtils.getClass(typeName.replace("[]", ""));
        } else if (Collection.class.isAssignableFrom(this.returnType)) {
            this.isArray = true;
            this.genericReturnType = getActualTypeArguments(this.method.getGenericReturnType());
        }
    }

    @SneakyThrows
    private Class<?> getActualTypeArguments(Type type) {
        final Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        final Type actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof ParameterizedType) {
            throw new RuntimeException("无法解析[嵌套泛型]的返回值[" + clazz.getName() + "." + method.getName() + "], 可用String类型接收");
        }
        return ClassUtils.getClass(actualTypeArgument.getTypeName());
    }

    /**
     * 处理注解
     */
    private void annotationHandle() {
        final RestClient client = clazz.getAnnotation(RestClient.class);
        if (client != null) {
            this.filterClass = client.filter() != AbstractRestFilter.class ? client.filter() : null;
            final RestMapping rmAnn = method.getAnnotation(RestMapping.class);
            if (rmAnn != null) {
                this.url = this.urlHandle(client.url(), rmAnn.url());
                this.restMethod = rmAnn.method();
                this.logger = LoggerFactory.getLogger(clazz.getName() + "." + method.getName());
                this.filterClass = rmAnn.filter() != AbstractRestFilter.class ? rmAnn.filter() : this.filterClass;
                this.ignoreFilter = rmAnn.ignoreFilter();
                this.createFilterInstance();
            }
        }
    }

    /**
     * 处理参数
     *
     * @param arguments
     */
    public void parameterHandle(Object[] arguments) {
        if (logger == null) {
            return;
        }
        final Parameter[] parameters = method.getParameters();
        for (Object parameter : arguments) {
            final String name = parameter.getClass().getName();
            if (RestMethod.class.getName().equals(name)) {
                this.restMethod = (RestMethod) parameter;
                break;
            }
        }
        final MethodParamVisitor mpv = new MethodParamVisitor();
        mpv.visit(parameters, arguments);
        this.url = mpv.getEncodeUrl(this.url);
        this.body = mpv.getBody();
        this.params = mpv.getParams();
        this.httpHeaders = mpv.getHeaders();
    }

    /**
     * 处理url
     *
     * @param clientUrl
     * @param mappingUrl
     * @return
     */
    private String urlHandle(String clientUrl, String mappingUrl) {
        if (StringUtils.isBlank(clientUrl)) {
            return mappingUrl;
        }
        final String lowerMapping = mappingUrl.toLowerCase();
        if (lowerMapping.startsWith("http:") || lowerMapping.startsWith("https:")) {
            return mappingUrl;
        }
        return clientUrl + mappingUrl;
    }

    @SneakyThrows
    private void createFilterInstance() {
        if (ignoreFilter || filterClass == null) {
            return;
        }
        final String name = filterClass.getName();
        if (name.equals(AbstractRestFilter.class.getName()) || name.equals(RestFilter.class.getName())) {
            return;
        }
        RestFilter rf = FILTERS.get(name);
        if (rf != null) {
            this.filter = rf;
            return;
        }
        synchronized (FILTERS) {
            rf = FILTERS.get(name);
            if (rf != null) {
                this.filter = rf;
                return;
            }
            this.filter = this.filterClass.newInstance();
            FILTERS.put(name, this.filter);
        }
    }

}