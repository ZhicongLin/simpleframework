package org.simpleframework.context.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
@Setter
@Getter
@AllArgsConstructor
public class SimpleMethodProxy {
    private Object bean;
    private Method method;
    private Object[] args;
    private MethodProxy methodProxy;
}