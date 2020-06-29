package org.simpleframework.http.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.annotation.PathParam;
import org.simpleframework.http.annotation.RestBody;
import org.simpleframework.http.annotation.RestHead;
import org.simpleframework.http.annotation.RestParam;
import org.simpleframework.http.annotation.RestURL;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.SneakyThrows;
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
 * 2020/6/18.1    linzc       2020/6/18     Create
 * </pre>
 * @date 2020/6/18
 */
@Slf4j
@Getter
public class RestParamVisitor {
    private static final String METHOD_VISITOR = "visit";
    private String url;
    private String body;
    private final Map<String, Object> params = new HashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    public void visit(Parameter[] parameters, Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return;
        }
        for (int i = 0; i < parameters.length; i++) {
            visits(parameters[i], arguments[i]);
        }
    }

    public void visits(Parameter parameter, Object value) {
        final Annotation[] annotations = parameter.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            final Annotation annotation = annotations[0];
            try {
                Class<?>[] classTypes = new Class[]{annotation.annotationType(), Object.class};
                final Method visitMethod = RestParamVisitor.class.getDeclaredMethod(METHOD_VISITOR, classTypes);
                visitMethod.setAccessible(true);
                Object[] args = new Object[]{annotation, value};
                visitMethod.invoke(this, args);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void visit(RestBody restBody, Object value) {
        if (value != null) {
            this.body = JSON.toJSONString(value);
            headers.put("content-type", "application/json;charset=utf-8");
        }
    }

    private void visit(RestURL restURL, Object value) {
        if (value instanceof String) {
            this.url = String.valueOf(value);
        }
    }

    private void visit(RestParam restParam, Object value) {
        final String key = restParam.value();
        if (value != null && StringUtils.isNotBlank(key)) {
            this.params.put(key, value);
        }
    }

    private void visit(PathParam pathParam, Object value) {
        final String key = pathParam.value();
        if (value != null && StringUtils.isNotBlank(key)) {
            pathParams.put("{" + key + "}", value.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private void visit(RestHead restHead, Object value) {
        if (value instanceof Map) {
            final Map<String, ?> headerMap = (Map<String, ?>) value;
            headerMap.forEach((k, v) -> this.headers.put(k, String.valueOf(v)));
        } else {
            final String key = restHead.value();
            if (StringUtils.isNotBlank(key)) {
                this.headers.put(key, String.valueOf(value));
            }
        }
    }

    @SneakyThrows
    public String getEncodeUrl(String url) {
        String result = url;
        if (StringUtils.isNotBlank(this.url)) {
            result = this.url;
        }

        if (pathParams.isEmpty()) {
            return result;
        }
        final Set<String> keySet = pathParams.keySet();
        for (String key : keySet) {
            final String replacement = pathParams.get(key);
            result = result.replace(key, URLEncoder.encode(replacement, "utf-8"));
        }
        return result;
    }
}