package org.simpleframework.http.io;

import org.simpleframework.http.proxy.RestObject;

public interface RestFilter {

    void before(RestObject restObject) throws Throwable;

    Object after(Object result, RestObject restObject) throws Throwable;
}
