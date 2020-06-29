package org.simpleframework.http.io;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.simpleframework.context.util.ClassUtils;
import org.simpleframework.http.RestContextHolder;
import org.simpleframework.http.annotation.RestClient;
import org.simpleframework.http.proxy.RestClientProxy;

/**
 * Description:
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
public class RestClassLoader {
    private Set<String> pkgSet = new HashSet<>();
    private Set<Class<?>> clientClasses = new HashSet<>();

    private RestClassLoader(String... pkgs) {
        this.pkgSet.addAll(Arrays.asList(pkgs));
    }

    public static void build(String... pkgs) {
        final RestClassLoader rcl = new RestClassLoader(pkgs);
        rcl.loaderClasses();

    }

    /**
     * 加载Rest客户端
     */
    public void loaderClasses() {
        for (String pkg : pkgSet) {
            final Set<Class<?>> classes = ClassUtils.scanPackageAndGetClasses(pkg);
            for (Class<?> aClass : classes) {
                final RestClient restClient = aClass.getAnnotation(RestClient.class);
                if (aClass.isInterface() && restClient != null) {
                    clientClasses.add(aClass);
                }
            }
        }

        for (Class<?> clientClass : this.clientClasses) {
            final RestClientProxy restClientProxy = new RestClientProxy(clientClass);
            RestContextHolder.addBean(clientClass, restClientProxy.getObject());
        }
    }
}