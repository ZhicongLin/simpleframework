package org.simpleframework.http.proxy.visitor;

import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.simpleframework.http.annotation.RestBody;
import org.simpleframework.http.proxy.MethodParamVisitor;

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
public class RestBodyVisitorImpl implements ParameterVisitor<RestBody> {

    @Override
    public void visitor(RestBody ann, Object value, MethodParamVisitor mpv) {
        if (value != null) {
            mpv.setBody(JSON.toJSONString(value));
            mpv.getHeaders().put("Content-Type", ContentType.APPLICATION_JSON.withCharset(Consts.UTF_8).toString());
        }
    }
}