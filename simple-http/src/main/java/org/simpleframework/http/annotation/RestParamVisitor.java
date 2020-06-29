package org.simpleframework.http.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;

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
    private Map<String, Object> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();

    public void visit(Parameter[] parameters, Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return;
        }
        for (int i = 0; i < parameters.length; i++) {
            visit(parameters[i], arguments[i]);
        }
    }

    public void visit(Parameter parameter, Object value) {
        final Annotation[] annotations = parameter.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            final Annotation annotation = annotations[0];
            try {
                Class<?>[] classTypes = new Class[]{annotation.annotationType(), Object.class};
                final Method visitMethod = RestParamVisitor.class.getMethod(METHOD_VISITOR, classTypes);
                Object[] args = new Object[]{annotation, value};
                visitMethod.invoke(this, args);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void visit(RestBody restBody, Object value) {
        if (value != null) {
            this.body = JSON.toJSONString(value);
        }
    }

    public void visit(RestURL restURL, Object value) {
        if (value instanceof String) {
            this.url = String.valueOf(value);
        }
    }

    public void visit(RestParam restParam, Object value) {
        final String key = restParam.value();
        if (value != null && StringUtils.isNotBlank(key)) {
            this.params.put(key, value);
        }
    }

    public void visit(RestHead restHead, Object value) {
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
}