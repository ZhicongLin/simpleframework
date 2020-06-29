package org.simpleframework.context.test;

import org.simpleframework.context.annotation.Bean;
import org.simpleframework.context.proxy.InitializedBean;

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
@Bean
public class TestService implements InitializedBean {

    public void testservice() {
        System.out.println("test");
    }

    @Override
    public void runner() {
        System.out.println("INIT -- test");
    }
}