package org.simpleframework.spring.http;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.http.HttpExecutor;
import org.simpleframework.http.io.RestClassLoader;
import org.simpleframework.http.proxy.RestObjectBuilder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * RestClient Registrar SpringContext
 */
@Slf4j
public class RestClientsRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Environment environment;

    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        //获取扫描包
        final Set<String> basePackages = this.getBasePackages(annotationMetadata);
        String[] packages = new String[basePackages.size()];
        int i = 0;
        for (String basePackage : basePackages) {
            packages[i] = basePackage;
            i++;
        }
        RestClassLoader.build(packages);
        //新增扫描器
        final RestClientScannerConfigurer scanner = new RestClientScannerConfigurer(this.environment);
        //扫描
        final long start = System.currentTimeMillis();
        final Set<RestClientGenericBeanDefinition> restClients = scanner.findCandidateComponents();
        for (RestClientGenericBeanDefinition candidateComponent : restClients) {
            this.registerRestClientBean(registry, candidateComponent);
        }
        log.info("Finished Rest Clients scanning in {}ms, Found {} clients interfaces.", (System.currentTimeMillis() - start), restClients.size());
    }

    @SneakyThrows
    private void registerRestClientBean(BeanDefinitionRegistry registry, RestClientGenericBeanDefinition raced) {
        final AbstractBeanDefinition definition = raced.getDefinition();
        final String factoryBeanName = Objects.requireNonNull(definition.getFactoryBeanName());
        registry.registerBeanDefinition(factoryBeanName, definition);
        log.info("Creating shared instance of singleton bean '{}'", factoryBeanName);
        final Class<?> beanClass = raced.getBeanClass();
        final Method[] methods = beanClass.getDeclaredMethods();
        for (Method method : methods) {
            RestObjectBuilder.newRestObject(method);
        }
    }

    private Set<String> getBasePackages(AnnotationMetadata annotationMetadata) {
        final Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableRestClients.class.getCanonicalName());
        final Set<String> basePackages = new HashSet<>();
        if (attributes != null) {
            basePackages.addAll(Arrays.asList((String[]) attributes.get(Constant.REST_CLIENT_VALUE)));
            basePackages.addAll(Arrays.asList((String[]) attributes.get(Constant.REST_CLIENT_BASE_PACKAGES)));
            final Class<?>[] classes = (Class<?>[]) attributes.get(Constant.REST_CLIENT_BASE_PACKAGE_CLASSES);
            Arrays.stream(classes).forEach(clazz -> basePackages.add(ClassUtils.getPackageName(clazz)));
        }
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(annotationMetadata.getClassName()));
        }
        return basePackages;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        // 重试次数配置
        final String property = this.environment.getProperty("simple.http.retry");
        if (StringUtils.isNotBlank(property)) {
            HttpExecutor.setRetryCount(Integer.parseInt(property.trim()));
        }
    }
}
