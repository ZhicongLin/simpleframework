package org.simpleframework.context.annotation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.simpleframework.context.io.ResourceLoader;
import org.simpleframework.context.util.AnnotationUtils;
import org.simpleframework.context.util.ClassUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Description: 类路径扫描器
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
@Slf4j
public class ClassPathBeanScanner extends AbstractBeanScanner implements ResourceLoader {

    private final Set<String> pkgScanSet = new HashSet<>();

    /**
     * 初始化加载器
     * @param beanScanClazz
     */
    public ClassPathBeanScanner(Class<?> beanScanClazz) {
        super(beanScanClazz);
        addPackage("org.simpleframework");
        addPackage(this.getParent().getName());
    }

    /**
     * 获取所有的Bean类型
     * @return
     */
    public Set<Class<?>> getBeanClasses() {
        final Set<Class<?>> beanClasses = new HashSet<>();
        final long l = System.currentTimeMillis();
        for (String pkg : this.pkgScanSet) {
            log.info(pkg);
            final Set<Class<?>> classes = ClassUtils.scanPackageAndGetClasses(pkg);
            for (Class<?> clazz : classes) {
                if (!clazz.isAnnotation() && !clazz.isInterface() && AnnotationUtils.isBeanClass(clazz)) {
                    beanClasses.add(clazz);
                }
            }
        }
        return beanClasses;
    }

    /**
     * 添加扫描的包
     * @param pkgs
     */
    public void addPackage(String... pkgs) {
        this.pkgScanSet.addAll(Arrays.asList(pkgs));
    }
}