package org.simpleframework.http.proxy.visitor;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.annotation.RestParam;
import org.simpleframework.http.proxy.MethodParamVisitor;

/**
 * Description: 处理RestParam的参数
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
public class RestParamVisitorImpl implements ParameterVisitor<RestParam> {

    @Override
    @SuppressWarnings({"unchecked"})
    public void visitor(RestParam restParam, Object value, MethodParamVisitor mpv) {
        if (value == null) {
            return;
        }
        final String key = restParam.value();
        if (StringUtils.isNotBlank(key)) {
            if (value.getClass().isArray()) {
                mpv.getParams().put(key, StringUtils.join((Object[]) value, ","));
            } else {
                final String name = value.getClass().getName();
                if (BASE_TYPE.contains(name)) {
                    mpv.getParams().put(key, value.toString());
                } else {
                    push(mpv.getParams(), key, value);
                }
            }
        } else if (value instanceof Map) {
            mpv.getParams().putAll((Map<? extends String, ?>) value);
        } else {
            push(mpv.getParams(), key, value);
        }

    }


}