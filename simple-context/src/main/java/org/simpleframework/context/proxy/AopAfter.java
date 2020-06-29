package org.simpleframework.context.proxy;

public interface AopAfter {

    void after(SimpleMethodProxy smp, Object result) throws Throwable;
}
