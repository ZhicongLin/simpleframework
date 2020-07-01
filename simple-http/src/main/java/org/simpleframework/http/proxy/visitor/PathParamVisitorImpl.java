package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.annotation.PathParam;

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
public class PathParamVisitorImpl implements ParameterVisitor {

    @Override
    public void visitor(Annotation ann, Object value, RestParamVisitor rpv) {
        PathParam pathParam= (PathParam)ann;
        final String key = pathParam.value();
        if (value != null && StringUtils.isNotBlank(key)) {
            rpv.getPathParams().put("{" + key + "}", value.toString());
        }
    }

}