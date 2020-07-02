package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.proxy.MethodParamVisitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public interface ParameterVisitor<T extends Annotation> {

    List<String> BASE_TYPE = Arrays.asList(
            Integer.class.getName(), int.class.getName(),
            Long.class.getName(), long.class.getName(),
            Double.class.getName(), double.class.getName(),
            Float.class.getName(), float.class.getName(),
            Boolean.class.getName(), boolean.class.getName(),
            String.class.getName()
    );

    Map<String, ParameterVisitor<?>> VISITOR_MAP = new HashMap<>();

    void visitor(T ann, Object value, MethodParamVisitor mpv);

    default void push(Map<String, Object> params, String key, Object value) {
        String prefix = key;
        if (StringUtils.isNotBlank(key)) {
            prefix += ".";
        }
        final JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(value));
        final String finalPrefix = prefix;
        jsonObject.forEach((k, v) -> {
            if (BASE_TYPE.contains(v.getClass().getName())) {
                params.put(finalPrefix + k, v);
            } else {
                push(params, finalPrefix + k, v);
            }
        });
    }
}
