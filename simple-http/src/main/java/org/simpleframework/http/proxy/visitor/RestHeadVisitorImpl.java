package org.simpleframework.http.proxy.visitor;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.annotation.RestHead;
import org.simpleframework.http.proxy.MethodParamVisitor;

/**
 * Description: 处理RestHead的参数
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
public class RestHeadVisitorImpl implements ParameterVisitor<RestHead> {

    @SuppressWarnings("unchecked")
    @Override
    public void visitor(RestHead restHead, Object value, MethodParamVisitor mpv) {
        if (value instanceof Map) {
            final Map<String, ?> headerMap = (Map<String, ?>) value;
            headerMap.forEach((k, v) -> mpv.getHeaders().put(k, String.valueOf(v)));
        } else {
            final String key = restHead.value();
            if (StringUtils.isNotBlank(key)) {
                mpv.getHeaders().put(key, String.valueOf(value));
            }
        }
    }
}