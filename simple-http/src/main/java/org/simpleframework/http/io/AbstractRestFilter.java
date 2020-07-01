package org.simpleframework.http.io;

import org.simpleframework.http.proxy.RestObject;

public class AbstractRestFilter implements RestFilter {

    public void before(RestObject restObject)  throws Throwable  {

    }

    @Override
    public Object after(Object result, RestObject restObject)  throws Throwable  {
        return result;
    }
}
