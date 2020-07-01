package org.simpleframework.http.proxy.visitor;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public interface ParameterVisitor {
    Map<String, ParameterVisitor> VISITOR_MAP = new HashMap<>();

    void visitor(Annotation ann, Object value, RestParamVisitor rpv);
}
