package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 *
 * @author linzc
 * @version 1.0
 *
 * <pre>
 * 修改记录:
 * 修改后版本        修改人     修改日期        修改内容
 * 2020/6/18.1    linzc       2020/6/18     Create
 * </pre>
 * @date 2020/6/18
 */
@Slf4j
@Getter
@Setter
public class MethodParamVisitor {
    private static final String METHOD_VISITOR = "visit";
    private String url;
    private String body;
    private final Map<String, Object> params = new HashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    public void visit(Parameter[] parameters, Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return;
        }
        for (int i = 0; i < parameters.length; i++) {
            final Annotation[] annotations = parameters[i].getAnnotations();
            if (annotations != null && annotations.length > 0) {
                final Annotation annotation = annotations[0];
                final ParameterVisitor visitor = VisitorEnum.getVisitor(annotation.annotationType().getName());
                if (visitor != null) {
                    visitor.visitor(annotation, arguments[i], this);
                }
            }
        }
    }

    @SneakyThrows
    public String getEncodeUrl(String url) {
        String result = url;
        if (StringUtils.isNotBlank(this.url)) {
            result = this.url;
        }

        if (pathParams.isEmpty()) {
            return result;
        }
        final Set<String> keySet = pathParams.keySet();
        for (String key : keySet) {
            final String replacement = pathParams.get(key);
            result = result.replace(key, URLEncoder.encode(replacement, "utf-8"));
        }
        return result;
    }
}