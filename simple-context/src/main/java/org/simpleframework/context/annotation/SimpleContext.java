package org.simpleframework.context.annotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class SimpleContext extends HashMap<String, Object> {

    public Set<Class<?>> getKeyClass() {
        Set<Class<?>> classSet = new HashSet<>();
        final Set<String> keySet = keySet();
        for (String key : keySet) {
            try {
                classSet.add(Class.forName(key));
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
        return classSet;
    }
}