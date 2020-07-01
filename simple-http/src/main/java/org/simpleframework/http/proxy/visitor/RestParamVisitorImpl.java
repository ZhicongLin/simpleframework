package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.annotation.RestParam;

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
    public void visitor(RestParam restParam, Object value, MethodParamVisitor mpv) {
        final String key = restParam.value();
        if (value != null && StringUtils.isNotBlank(key)) {
            mpv.getParams().put(key, value);
        }
    }
}