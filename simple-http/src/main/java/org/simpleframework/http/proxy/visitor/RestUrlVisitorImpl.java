package org.simpleframework.http.proxy.visitor;

import org.simpleframework.http.annotation.RestURL;
import org.simpleframework.http.proxy.MethodParamVisitor;
import org.simpleframework.http.proxy.visitor.exception.RestParameterNullValueException;
import org.simpleframework.http.proxy.visitor.exception.RestParameterTypeSuchException;

/**
 * Description: 处理RestURL的参数
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
public class RestUrlVisitorImpl implements ParameterVisitor<RestURL> {

    @Override
    public void visitor(RestURL restURL, Object value, MethodParamVisitor mpv) {
        if (value == null) {
            if (restURL.require()) {
                throw new RestParameterNullValueException("RestURL");
            }
            return;
        }
        if (value instanceof String) {
            mpv.setUrl(String.valueOf(value));
        } else {
            throw new RestParameterTypeSuchException("RestURL Value Type Must Be [java.lang.String]");
        }
    }
}