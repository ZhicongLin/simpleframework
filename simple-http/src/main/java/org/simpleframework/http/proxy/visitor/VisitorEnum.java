package org.simpleframework.http.proxy.visitor;

import org.simpleframework.http.annotation.PathParam;
import org.simpleframework.http.annotation.RestBody;
import org.simpleframework.http.annotation.RestHead;
import org.simpleframework.http.annotation.RestParam;
import org.simpleframework.http.annotation.RestURL;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public enum VisitorEnum {
    Body(RestBody.class.getName(), RestBodyVisitorImpl.class),
    Head(RestHead.class.getName(), RestHeadVisitorImpl.class),
    Url(RestURL.class.getName(), RestUrlVisitorImpl.class),
    Param(RestParam.class.getName(), RestParamVisitorImpl.class),
    Path(PathParam.class.getName(), PathParamVisitorImpl.class);
    private final String key;
    private final Class<?> clazz;

    public static ParameterVisitor getVisitor(String key) {
        final VisitorEnum visitorEnum = VisitorEnum.value(key);
        if (visitorEnum == null) {
            return null;
        }
        final Class<?> clazz = visitorEnum.clazz;
        final String className = clazz.getName();
        ParameterVisitor parameterVisitor = ParameterVisitor.VISITOR_MAP.get(className);
        if (parameterVisitor != null) {
            return parameterVisitor;
        }
        synchronized (ParameterVisitor.VISITOR_MAP) {
            parameterVisitor = ParameterVisitor.VISITOR_MAP.get(className);
            if (parameterVisitor != null) {
                return parameterVisitor;
            }
            try {
                parameterVisitor = (ParameterVisitor) clazz.newInstance();
                ParameterVisitor.VISITOR_MAP.put(className, parameterVisitor);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
        return parameterVisitor;
    }

    static VisitorEnum value(String key) {
        final VisitorEnum[] values = VisitorEnum.values();
        for (VisitorEnum value : values) {
            final boolean equals = value.key.equals(key);
            if (value.key.equals(key)) {
                return value;
            }
        }
        return null;
    }
}
