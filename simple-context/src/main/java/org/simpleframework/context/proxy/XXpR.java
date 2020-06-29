package org.simpleframework.context.proxy;

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
 * 2020/6/16.1    linzc       2020/6/16     Create
 * </pre>
 * @date 2020/6/16
 */
@Bean
public class XXpR implements InitializedBean{
    @Override
    public void runner() {
        System.out.println("=========");
    }
}