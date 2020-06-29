package org.simpleframework.context.test;

import org.simpleframework.context.annotation.Bean;

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
public class TestController {
    TestService testService;
    @Haha
    public void test() {
        testService.testservice();
        System.out.println(TestController.class);
    }
}