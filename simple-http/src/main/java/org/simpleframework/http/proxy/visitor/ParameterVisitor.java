package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public interface ParameterVisitor<T extends Annotation> {

    Map<String, ParameterVisitor<?>> VISITOR_MAP = new HashMap<>();

    void visitor(T ann, Object value, MethodParamVisitor mpv);
}
