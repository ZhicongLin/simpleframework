package org.simpleframework.http;

import java.lang.reflect.Array;
import java.util.List;

import com.alibaba.fastjson.JSON;

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
    private final T[] array;
    private int index = 0;

    /**
     * 构建数组
     *
     * @param clazz
     * @param size
     */
    public ArrayGeneric(Class<T> clazz, int size) {
        this.array = createQueue(clazz, size);
    }

    @SuppressWarnings({"unchecked"})
    private T[] createQueue(Class<T> clazz, int size) {
        return (T[]) Array.newInstance(clazz, size);
    }

    public void add(T u) {
        this.array[this.index++] = u;
    }

    /**
     * 将 fastjson的JSONArray转化为泛型列表
     *
     * @param <T>        泛型
     * @param jsonArray  源数据
     * @param returnType 泛型类
     * @return list
     */
    public static <T> T[] toArray(String jsonArray, Class<T> returnType) {
        final List<T> objectList = JSON.parseArray(jsonArray, returnType);
        return toArray(objectList, returnType);
    }

    /**
     * 将 objectList转化为泛型数组
     *
     * @param <T>        泛型
     * @param objectList 源数据
     * @param returnType 泛型类
     * @return list
     */
    private static <T> T[] toArray(List<T> objectList, Class<T> returnType) {
        final ArrayGeneric<T> arrayGeneric = new ArrayGeneric<>(returnType, objectList.size());
        for (T element : objectList) {
            arrayGeneric.add(element);
        }
        return arrayGeneric.getArray();
    }
}