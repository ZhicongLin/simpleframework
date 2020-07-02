package org.simpleframework.http.io;

import org.simpleframework.http.proxy.RestObject;

public interface RestFilter {
    // 请求之前
    void before(RestObject restObject) throws Throwable;

    // 成功之后
    Object after(Object result, RestObject restObject) throws Throwable;
}
