package org.simpleframework.context.test;

import org.simpleframework.context.proxy.AopAfter;
import org.simpleframework.context.proxy.AopBefore;
import org.simpleframework.context.proxy.SimpleMethodProxy;
import org.simpleframework.context.proxy.annotation.After;
import org.simpleframework.context.proxy.annotation.AopListener;
import org.simpleframework.context.proxy.annotation.Before;

/**
 * Description:
 *
 * @author linzc
 * @version 1.0
 *
 * <pre>
 * 修改记录:
 * 修改后版本        修改人     修改日期        修改内容
 * 2020/6/15.1    linzc       2020/6/15     Create
 * </pre>
 * @date 2020/6/15
 */
@AopListener(Haha.class)
public class AopTest implements AopBefore, AopAfter {
    @Override
    @Before
    public void after(SimpleMethodProxy smp, Object result) throws Throwable {
        System.out.println("[Proxy]一些后置处理");
    }

    @Override
    @After(execute = false)
    public void before(SimpleMethodProxy smp) throws Throwable {

        System.out.println("[Proxy]一些前置处理");
    }
}