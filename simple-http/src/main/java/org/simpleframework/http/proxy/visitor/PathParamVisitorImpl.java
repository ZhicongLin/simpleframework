package org.simpleframework.http.proxy.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.annotation.PathParam;
import org.simpleframework.http.proxy.MethodParamVisitor;
import org.simpleframework.http.proxy.visitor.exception.RestParameterNullValueException;

import com.alibaba.fastjson.JSON;

/**
 * Description: 处理PathParam的参数
 *
 * @author linzc
 * @version 1.0
 *
 * <pre>
 * 修改记录:
 * 修改后版本        修改人     修改日期        修改内容
 * 2020/7/1.1    linzc       2020/7/1     Create
 * </pre>
 * @date 2020/7/1
 */
public class PathParamVisitorImpl implements ParameterVisitor<PathParam> {

    @Override
    public void visitor(PathParam pathParam, Object value, MethodParamVisitor mpv) {
        final String key = pathParam.value();
        if (value == null) {
            if (pathParam.require()) {
                throw new RestParameterNullValueException("PathParam");
            }
            return;
        }
        if (BASE_TYPE.contains(value.getClass().getName())) {
            if (StringUtils.isNotBlank(key)) {
                mpv.getPathParams().put(getKey(key), value.toString());
            }
            return;
        }
        final Map<String, Object> param = new HashMap<>();
        this.push(param, "", value);
        param.forEach((k, v) -> mpv.getPathParams().put(getKey(k), String.valueOf(v)));
        if (StringUtils.isNotBlank(key)) {
            final Set<String> keySet = param.keySet();
            if (!keySet.contains(key)) {
                mpv.getPathParams().put(getKey(key), JSON.toJSONString(value));
            }
        }
    }

    private String getKey(String key) {
        return "{" + key + "}";
    }

}