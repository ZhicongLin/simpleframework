package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;

import org.simpleframework.http.annotation.RestURL;

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
public class RestUrlVisitorImpl implements ParameterVisitor {

    @Override
    public void visitor(Annotation ann, Object value, MethodParamVisitor mpv) {
        RestURL restURL = (RestURL) ann;
        if (value instanceof String) {
            mpv.setUrl(String.valueOf(value));
        }
    }
}