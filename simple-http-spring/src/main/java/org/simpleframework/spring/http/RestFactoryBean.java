package org.simpleframework.spring.http;

import org.simpleframework.http.RestContextHolder;
import org.springframework.beans.factory.FactoryBean;

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
 * 2020/6/24.1    linzc       2020/6/24     Create
 * </pre>
 * @date 2020/6/24
 */
@Setter
@Getter
public class RestFactoryBean<T> implements FactoryBean<T> {

    private Class<T> objectType;

    @Override
    public T getObject() throws Exception {
        return RestContextHolder.getBean(objectType);
    }

}