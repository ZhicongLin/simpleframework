package org.simpleframework.context.io;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.simpleframework.context.util.ClassUtils;

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
public interface ResourceLoader {

    ClassLoader getClassLoader();

    String getScanPackage();
}