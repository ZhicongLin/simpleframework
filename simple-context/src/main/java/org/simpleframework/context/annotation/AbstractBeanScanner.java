package org.simpleframework.context.annotation;

import java.util.HashSet;
import java.util.Set;

import org.simpleframework.context.io.ResourceLoader;
import org.simpleframework.context.util.ClassUtils;

import lombok.Getter;


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
@Getter
public abstract class AbstractBeanScanner implements ResourceLoader {

    private final Class<?> beanScanClazz;

    private String scanPackage;

    private final Package parent;

    protected final Set<Package> packages = new HashSet<>();

    public AbstractBeanScanner(Class<?> beanScanClazz) {
        this.beanScanClazz = beanScanClazz;
        this.scanPackage = "";
        this.parent = beanScanClazz.getPackage();
        this.packages.add(parent);
        this.scanPackage = ClassUtils.convertClassNameToResourcePath(parent.getName());
    }

    public ClassLoader getClassLoader() {
        return beanScanClazz.getClassLoader();
    }

    @Override
    public String getScanPackage() {
        return scanPackage;
    }
}