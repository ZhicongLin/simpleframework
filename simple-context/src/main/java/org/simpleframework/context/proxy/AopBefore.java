package org.simpleframework.context.proxy;

public interface AopBefore {

    void before(SimpleMethodProxy smp) throws Throwable;

}
