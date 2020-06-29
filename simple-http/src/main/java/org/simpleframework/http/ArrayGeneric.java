package org.simpleframework.http;

import java.lang.reflect.Array;

import lombok.Getter;

/**
 * Description: 转换泛型数组
 *
 * @author linzc
 * @version 1.0
 *
 * <pre>
 * 修改记录:
 * 修改后版本        修改人     修改日期        修改内容
 * 2020/6/17.1    linzc       2020/6/17     Create
 * </pre>
 * @date 2020/6/17
 */
@Getter
public class ArrayGeneric<T> {
    private final T[] queue;
    private int index = 0;


    public ArrayGeneric(Class<T> clazz, int size) {
        queue = createQueue(clazz, size);
    }

    @SuppressWarnings({"unchecked"})
    private T[] createQueue(Class<T> clazz, int size) {
        return (T[]) Array.newInstance(clazz, size);
    }

    public void add(T u) {
        queue[index++] = u;
    }

}