package org.simpleframework.context.proxy;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

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
@Data
public class MethodAdvices {

    List<Class<?>> aopBefores = new ArrayList<>();
    List<Class<?>> aopAfters = new ArrayList<>();

    public void addBefore(Class<?> before) {
        aopBefores.add(before);
    }

    public void addAfter(Class<?> after) {
        aopAfters.add(after);
    }
}