package org.simpleframework.context.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.extern.slf4j.Slf4j;

/**
 * Description: 类工具
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
public class ClassUtils {
    //已加载的包
    private static final Set<String> LOADED_PKG = new HashSet<>();

    /**
     * 类名替换为/
     *
     * @param classPath
     * @return
     */
    public static String convertClassNameToResourcePath(String classPath) {
        assert classPath != null;
        return classPath.replace('.', '/');
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param packageName
     * @return
     */
    public static Set<Class<?>> scanPackageAndGetClasses(String packageName) {
        //第一个class类的集合
        final Set<Class<?>> classes = new HashSet<>();
        //获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things
        try {
            final Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去
            while (dirs.hasMoreElements()) {
                //获取下一个元素
                URL url = dirs.nextElement();
                //得到协议的名称
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    //获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, true, classes);
                } else if ("jar".equals(protocol)) {
                    findJarClasses(packageName, classes, packageDirName, url);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return classes;
    }

    /**
     * 如果是jar包文件 加载jar的文件信息
     */
    private static void findJarClasses(String packageName, Set<Class<?>> classes, String packageDirName, URL url) {
        //如果是jar包文件  定义一个JarFile
        //获取jar
        final JarFile jar;
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return;
        }
        //从此jar包 得到一个枚举类
        final Enumeration<JarEntry> entries = jar.entries();
        //同样的进行循环迭代
        while (entries.hasMoreElements()) {
            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            final JarEntry entry = entries.nextElement();
            String name = entry.getName();
            //如果是以/开头的
            if (name.charAt(0) == '/') {
                //获取后面的字符串
                name = name.substring(1);
            }
            //如果前半部分和定义的包名不相同
            if (!name.startsWith(packageDirName)) {
                continue;
            }
            //如果前半部分和定义的包名相同
            final int idx = name.lastIndexOf('/');
            //如果以"/"结尾 是一个包
            if (idx != -1) {
                //获取包名 把"/"替换成"."
                packageName = name.substring(0, idx).replace('/', '.');
            }
            //如果可以迭代下去 并且是一个包
            if (idx != -1 && name.endsWith(".class") && !entry.isDirectory()) {
                //如果是一个.class文件 而且不是目录
                String className = name.substring(packageName.length() + 1, name.length() - 6);
                //去掉后面的".class" 获取真正的类名
                addClasses(packageName, classes, className);
            }
        }


    }

    //添加到classes
    private static void addClasses(String packageName, Set<Class<?>> classes, String className) {
        try {
            //添加到classes
            classes.add(Class.forName(packageName + '.' + className));
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        //获取此包的目录 建立一个File
        File dir = new File(packagePath);
        //如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //如果存在 就获取包下的所有文件 包括目录
        //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] listFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        if (listFiles == null) {
            return;
        }
        //循环所有文件
        for (File file : listFiles) {
            //如果是目录 则继续扫描
            if (file.isDirectory()) {
                final String pkgDir = packageName + "." + file.getName();
                findAndAddClassesInPackageByFile(pkgDir, file.getAbsolutePath(), recursive, classes);
                continue;
            }
            //如果是java类文件 去掉后面的.class 只留下类名
            final String className = file.getName().substring(0, file.getName().length() - 6);
            addClasses(packageName, classes, className);
        }
    }
}