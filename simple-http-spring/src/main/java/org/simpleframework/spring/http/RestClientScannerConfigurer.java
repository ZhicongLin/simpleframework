package org.simpleframework.spring.http;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simpleframework.http.RestContextHolder;
import org.simpleframework.http.annotation.RestClient;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Getter
public class RestClientScannerConfigurer {
    private static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";
    private Environment environment;

    private ResourcePatternResolver resourcePatternResolver;
    private MetadataReaderFactory metadataReaderFactory;

    public RestClientScannerConfigurer(Environment environment) {
        this.environment = environment;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        this.metadataReaderFactory = new CachingMetadataReaderFactory();
    }

    public Set<RestClientGenericBeanDefinition> findCandidateComponents() {
        Set<RestClientGenericBeanDefinition> beanDefinitions = new HashSet<>();
        final Map<String, Object> context = RestContextHolder.getContext();
        final Set<String> keySet = context.keySet();
        for (String key : keySet) {
            scanRestClients(beanDefinitions, key);
        }
        return beanDefinitions;
    }

    /**
     * 扫描包，并加载Bean
     *
     * @param beanDefinitions
     * @param className
     */
    private void scanRestClients(Set<RestClientGenericBeanDefinition> beanDefinitions, String className) {
        try {
            final Resource[] resources = getResourcePatternResolver().getResources(this.getClassSourcePath(className));
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    log.trace("Ignored because not readable: " + resource);
                    continue;
                }
                final RestClientGenericBeanDefinition beanDefinition = this.loadBeanDefinition(resource);
                if (beanDefinition == null) {
                    continue;
                }
                beanDefinitions.add(beanDefinition);
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
    }

    /**
     * 加载bean
     *
     * @param resource
     * @return
     */
    private RestClientGenericBeanDefinition loadBeanDefinition(Resource resource) {
        try {
            MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
            final AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
            final boolean isRestClient = annotationMetadata.hasAnnotation(RestClient.class.getName());
            if (!isRestClient) {
                return null;
            }
            final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RestFactoryBean.class);
            final Class<?> beanClass = Class.forName(metadataReader.getClassMetadata().getClassName());
            builder.addPropertyValue("objectType", beanClass);
            final AbstractBeanDefinition definition = builder.getBeanDefinition();
            definition.setAutowireCandidate(true);
            final Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(RestClient.class.getCanonicalName());
            if (attributes != null) {
                final Boolean enableFallback = this.environment.getProperty("cgcg.rest.fallback.enable", Boolean.class);
                final Object fallback = attributes.get(Constant.PROXY_FALLBACK_KEY);
                if ((enableFallback == null || enableFallback) && fallback != Void.class) {
                    final Object bean = ((Class<?>) fallback).newInstance();
                    builder.addPropertyValue(Constant.PROXY_FALLBACK_BEAN_KEY, bean);
                }
            }
            final String className = metadataReader.getClassMetadata().getClassName();
            definition.setFactoryBeanName(className);
            return new RestClientGenericBeanDefinition(annotationMetadata, definition);
        } catch (Throwable ex) {
            throw new BeanDefinitionStoreException(
                    "Failed to read rest client class: " + resource, ex);
        }
    }

    /**
     * 用"/"替换包路径中"."
     *
     * @param className
     * @return
     */
    private String getClassSourcePath(String className) {
        final String path = ClassUtils.convertClassNameToResourcePath(className);
        return ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + path + ".class";
    }
}