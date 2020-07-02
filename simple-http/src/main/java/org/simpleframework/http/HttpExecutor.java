package org.simpleframework.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.simpleframework.http.annotation.RestMethod;
import org.simpleframework.http.io.RestFilter;
import org.simpleframework.http.proxy.RestObject;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * Description: Http客户端工具
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
@Slf4j
public final class HttpExecutor {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String AND_SYMBOL = "&";
    private static final String PROGRAM_SYMBOL = "?";

    /**
     * 执行请求
     *
     * @param restObject
     * @return
     * @throws IOException
     */
    public static Object execute(RestObject restObject) throws Throwable {
        final long start = System.currentTimeMillis();
        final Logger logger = restObject.getLogger();
        final RestFilter filter = restObject.getFilter();
        if (filter != null) {
            filter.before(restObject);
        }
        final HttpRequestBase requestBase = getHttpRequestBase(restObject);

        try {
            logger.info(requestBase.toString());
            if (!restObject.getParams().isEmpty()) {
                logger.info("> Parameter {}", restObject.getParams());
            }

            if (RestMethod.STREAM.equals(restObject.getRestMethod())
                    || InputStream.class.isAssignableFrom(restObject.getReturnType())) {
                InputStream is = getInputStream(requestBase, logger);
                final Class<?> grt = restObject.getGenericReturnType();
                if (restObject.getReturnType().isArray() && (byte.class.isAssignableFrom(grt) || Byte.class.isAssignableFrom(grt))) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    return filter == null ? bytes : filter.after(bytes, restObject);
                }
                return filter == null ? is : filter.after(is, restObject);
            }

            final Object result = executeClient(requestBase, restObject);
            return filter == null ? result : filter.after(result, restObject);
        } finally {
            logger.info("END {} {}s", requestBase.getURI(), (System.currentTimeMillis() - start) / 1000.0);
        }
    }

    private static InputStream getInputStream(HttpRequestBase requestBase, Logger logger) throws IOException {
        final CloseableHttpResponse response = getResponse(requestBase, logger);
        final HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    private static CloseableHttpResponse getResponse(HttpRequestBase requestBase, Logger logger) throws IOException {
        if (logger.isDebugEnabled()) {
            final Header[] headers = requestBase.getAllHeaders();
            for (Header header : headers) {
                logger.debug("> {} = {}", header.getName(), header.getValue());
            }
        }
        final CloseableHttpClient client = HttpClients.createDefault();
        final CloseableHttpResponse response = client.execute(requestBase);
        if (logger.isDebugEnabled()) {
            final Header[] allHeaders = response.getAllHeaders();
            for (Header hd : allHeaders) {
                logger.debug("< {} = {}", hd.getName(), hd.getValue());
            }
        }
        return response;
    }

    private static Object executeClient(HttpRequestBase requestBase, RestObject restObject) throws IOException {
        final CloseableHttpResponse response = getResponse(requestBase, restObject.getLogger());
        final HttpEntity entity = response.getEntity();
        final int statusCode = response.getStatusLine().getStatusCode();
        final String result = EntityUtils.toString(entity, DEFAULT_CHARSET);
        response.close();
        if (statusCode != HttpStatus.SC_OK) {
            restObject.getLogger().error(result);
            throw new IllegalArgumentException(result);
        }
        restObject.getLogger().trace("< Result = {} ", result);
        return castObjectToReturnType(result, restObject);
    }

    private static Object castObjectToReturnType(String result, RestObject restObject) {
        final Class<?> returnType = restObject.getReturnType();
        if (returnType.equals(String.class) || Object.class.equals(returnType)) {
            return result;
        } else if (Integer.class.isAssignableFrom(returnType) || int.class.isAssignableFrom(returnType)) {
            return Integer.parseInt(result);
        } else if (Long.class.isAssignableFrom(returnType) || long.class.isAssignableFrom(returnType)) {
            return Long.parseLong(result);
        } else if (Float.class.isAssignableFrom(returnType) || float.class.isAssignableFrom(returnType)) {
            return Float.parseFloat(result);
        } else if (Double.class.isAssignableFrom(returnType) || double.class.isAssignableFrom(returnType)) {
            return Double.parseDouble(result);
        } else if (Boolean.class.isAssignableFrom(returnType) || boolean.class.isAssignableFrom(returnType)) {
            return Boolean.valueOf(result);
        } else if (returnType.isArray()) {
            return ArrayGeneric.toArray(result, restObject.getGenericReturnType());
        } else if (restObject.isArray()) {
            return JSON.parseArray(result, restObject.getGenericReturnType());
        }
        return JSON.parseObject(result, returnType);
    }

    /**
     * 创建请求方式
     *
     * @param restObject
     * @return
     */
    private static HttpRequestBase getHttpRequestBase(RestObject restObject) throws Exception {
        final String url = restObject.getUrl();
        final Map<String, Object> params = restObject.getParams();
        final String body = restObject.getBody();
        final Class<HttpExecutor> httpExecutorClass = HttpExecutor.class;
        final Method dm = httpExecutorClass.getDeclaredMethod(restObject.getRestMethod().name().toLowerCase(), RestObject.class);
        final HttpRequestBase hg = (HttpRequestBase) dm.invoke(null, restObject);
        final Map<String, String> httpHeaders = restObject.getHttpHeaders();
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            httpHeaders.forEach(hg::addHeader);
        }
        return hg;
    }

    private static HttpDelete delete(RestObject restObject) {
        return new HttpDelete(createRequestUrl(restObject.getUrl(), restObject.getParams()));
    }

    private static HttpGet get(RestObject restObject) {
        return new HttpGet(createRequestUrl(restObject.getUrl(), restObject.getParams()));
    }

    private static HttpGet stream(RestObject restObject) {
        return get(restObject);
    }

    private static HttpPost post(RestObject restObject) {
        final HttpPost httpPost = new HttpPost(restObject.getUrl());
        if (restObject.getParams().isEmpty()) {
            return httpPost;
        }
        if (StringUtils.isNotBlank(restObject.getBody())) {
            httpPost.setEntity(new StringEntity(restObject.getBody(), DEFAULT_CHARSET));
        } else {
            httpPost.setEntity(createMultipartEntity(restObject.getParams()));
        }
        return httpPost;
    }

    private static HttpPut put(RestObject restObject) {
        final HttpPut httpPut = new HttpPut(restObject.getUrl());
        if (restObject.getParams().isEmpty()) {
            return httpPut;
        }
        if (StringUtils.isNotBlank(restObject.getBody())) {
            httpPut.setEntity(new StringEntity(restObject.getBody(), DEFAULT_CHARSET));
        } else {
            httpPut.setEntity(createMultipartEntity(restObject.getParams()));
        }
        return httpPut;
    }

    private static HttpPatch patch(RestObject restObject) {
        final HttpPatch httpPatch = new HttpPatch(restObject.getUrl());
        if (restObject.getParams().isEmpty()) {
            return httpPatch;
        }
        if (StringUtils.isNotBlank(restObject.getBody())) {
            httpPatch.setEntity(new StringEntity(restObject.getBody(), DEFAULT_CHARSET));
        } else {
            httpPatch.setEntity(createMultipartEntity(restObject.getParams()));
        }
        return httpPatch;
    }

    private static HttpTrace trace(RestObject restObject) {
        return new HttpTrace(createRequestUrl(restObject.getUrl(), restObject.getParams()));
    }

    private static HttpHead head(RestObject restObject) {
        return new HttpHead(createRequestUrl(restObject.getUrl(), restObject.getParams()));
    }

    private static HttpEntity createMultipartEntity(Map<String, Object> param) {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        param.forEach((key, value) -> builder.addPart(createBodyPart(key, value)));
        return builder.build();
    }

    private static FormBodyPart createBodyPart(String key, Object value) {
        ContentBody body;
        if (value instanceof File) {
            body = new FileBody((File) value);
        } else if (value instanceof InputStream) {
            body = new InputStreamBody((InputStream) value, UUID.randomUUID().toString());
        } else {
            body = new StringBody(value.toString(), ContentType.create("text/plain", Consts.UTF_8));
        }
        return FormBodyPartBuilder.create(key, body).build();
    }

    private static String createRequestUrl(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        final StringBuilder sbd = new StringBuilder();
        params.forEach((key, value) -> {
            try {
                sbd.append(key).append("=").append(URLEncoder.encode(value.toString(), DEFAULT_CHARSET)).append(AND_SYMBOL);
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        });
        return url + (url.contains(PROGRAM_SYMBOL) ? AND_SYMBOL : PROGRAM_SYMBOL) + sbd.substring(0, sbd.length() - 1);
    }

}