package org.simpleframework.context;

import org.simpleframework.context.annotation.BeanScan;
import org.simpleframework.context.test.TestController;

@BeanScan({"org.simpleframework.abc"})
public class ContextApplicationTests {

    public static void main(String[] args) throws Exception {
        SimpleContainerLoader.run(ContextApplicationTests.class);

        final TestController bean = SimpleContextHolder.getBean(TestController.class);
        bean.test();
    }

}
