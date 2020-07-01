package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;

import org.simpleframework.http.annotation.RestBody;

import com.alibaba.fastjson.JSON;

/**
 * Description: 处理RestBody的参数
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
public class RestBodyVisitorImpl implements ParameterVisitor {

    @Override
    public void visitor(Annotation ann, Object value, RestParamVisitor rpv) {
        RestBody restBody = (RestBody) ann;
        if (value != null) {
            rpv.setBody(JSON.toJSONString(value));
            rpv.getHeaders().put("content-type", "application/json;charset=utf-8");
        }
    }
}