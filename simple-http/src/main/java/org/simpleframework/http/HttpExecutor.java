package org.simpleframework.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
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
        final RestFilter filter = restObject.getFilter();
        if (filter != null) {
            filter.before(restObject);
        }
        final HttpRequestBase requestBase = getHttpRequestBase(restObject.getUrl(), restObject.getBody(),
                restObject.getParams(), restObject.getRestMethod(), restObject.getHttpHeaders());
        final Logger logger = restObject.getLogger();
        logger.info(requestBase.toString());
        logger.info("Parameter {}", restObject.getParams());
        if (RestMethod.FILE.equals(restObject.getRestMethod())) {
            final InputStream inputStream = getInputStream(requestBase);
            if (filter != null) {
                return filter.after(inputStream, restObject);
            }
            return inputStream;
        }
        final String result = executeClient(requestBase, logger);
        final Object o = castResultType(result, restObject);
        if (filter != null) {
            return filter.after(o, restObject);
        }
        return o;
    }

    private static InputStream getInputStream(HttpRequestBase requestBase) throws IOException {
        final CloseableHttpClient client = HttpClients.createDefault();
        final CloseableHttpResponse response = client.execute(requestBase);
        final HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    private static String executeClient(HttpRequestBase requestBase, Logger logger) throws IOException {
        if (logger.isDebugEnabled()) {
            final Header[] headers = requestBase.getAllHeaders();
            for (Header header : headers) {
                logger.debug("{}={}", header.getName(), header.getValue());
            }
        }

        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final CloseableHttpResponse response = httpClient.execute(requestBase);

        if (logger.isDebugEnabled()) {
            final Header[] allHeaders = response.getAllHeaders();
            for (Header hd : allHeaders) {
                logger.debug("{}={}", hd.getName(), hd.getValue());
            }
        }

        final HttpEntity entity = response.getEntity();
        final int statusCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(entity, DEFAULT_CHARSET);
        response.close();
        if (statusCode != HttpStatus.SC_OK) {
            logger.error(result);
            throw new IllegalArgumentException(result);
        }
        logger.trace("< {} >", result);
        return result;
    }

    private static Object castResultType(String result, RestObject restObject) {
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
            return convertJsonToArray(result, restObject.getGenericReturnType());
        } else if (restObject.isArray()) {
            return JSON.parseArray(result, restObject.getGenericReturnType());
        }
        return JSON.parseObject(result, returnType);
    }

    /**
     * 创建请求方式
     *
     * @param url
     * @param body
     * @param params
     * @param restMethod
     * @param httpHeaders
     * @return
     */
    private static HttpRequestBase getHttpRequestBase(String url, String body, Map<String, Object> params, RestMethod restMethod, Map<String, String> httpHeaders) {
        HttpRequestBase hg = null;
        switch (restMethod) {
            case GET:
            case FILE:
                hg = get(url, params);
                break;
            case POST:
                hg = post(url, params, body);
                break;
            case DELETE:
                hg = delete(url, params);
                break;
            case PUT:
                hg = put(url, params, body);
                break;
            case PATCH:
                hg = patch(url, params, body);
                break;
            case HEAD:
                hg = head(url, params);
                break;
            case TRACE:
                hg = trace(url, params);
                break;
        }
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            httpHeaders.forEach(hg::addHeader);
        }
        return hg;
    }

    private static HttpDelete delete(String url, Map<String, Object> params) {
        url = createRequestUrl(url, params);
        return new HttpDelete(url);
    }

    private static HttpGet get(String url, Map<String, Object> params) {
        url = createRequestUrl(url, params);
        return new HttpGet(url);
    }

    private static HttpPost post(String url, Map<String, Object> params, String body) {
        final HttpPost httpPost = new HttpPost(url);
        if (params.isEmpty()) {
            return httpPost;
        }
        if (StringUtils.isNotBlank(body)) {
            httpPost.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        } else {
            httpPost.setEntity(createMultipartEntity(params));
        }
        return httpPost;
    }

    private static HttpPut put(String url, Map<String, Object> params, String body) {
        final HttpPut httpPut = new HttpPut(url);
        if (params.isEmpty()) {
            return httpPut;
        }
        if (StringUtils.isNotBlank(body)) {
            httpPut.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        } else {
            httpPut.setEntity(createMultipartEntity(params));
        }
        return httpPut;
    }

    private static HttpPatch patch(String url, Map<String, Object> params, String body) {
        final HttpPatch httpPatch = new HttpPatch(url);
        if (params.isEmpty()) {
            return httpPatch;
        }
        if (StringUtils.isNotBlank(body)) {
            httpPatch.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        } else {
            httpPatch.setEntity(createMultipartEntity(params));
        }
        return httpPatch;
    }

    private static HttpTrace trace(String url, Map<String, Object> params) {
        url = createRequestUrl(url, params);
        return new HttpTrace(url);
    }

    private static HttpHead head(String url, Map<String, Object> params) {
        url = createRequestUrl(url, params);
        return new HttpHead(url);
    }

    private static HttpEntity createMultipartEntity(Map<String, Object> param) {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        param.forEach((key, value) -> {
            try {
                builder.addPart(createBodyPart(key, value));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        });
        return builder.build();
    }

    private static FormBodyPart createBodyPart(String key, Object value) throws UnsupportedEncodingException {
        ContentBody body;
        if (value instanceof File) {
            body = new FileBody((File) value);
        } else if (value instanceof InputStream) {
            body = new InputStreamBody((InputStream) value, UUID.randomUUID().toString());
        } else {
            body = new StringBody(value.toString());
        }
        final FormBodyPartBuilder fileBuilder = FormBodyPartBuilder.create(key, body);
        return fileBuilder.build();
    }

    private static String createRequestUrl(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sbd = new StringBuilder();
        params.forEach((key, value) -> sbd.append(key).append("=").append(value).append(AND_SYMBOL));
        final String substr = sbd.substring(0, sbd.length() - 1);
        if (url.contains(PROGRAM_SYMBOL)) {
            url = url + AND_SYMBOL + substr;
        } else {
            url = url + PROGRAM_SYMBOL + substr;
        }
        return url;
    }

    /**
     * 将 fastjson的JSONArray转化为泛型列表
     *
     * @param <T>        泛型
     * @param json       源数据
     * @param returnType 泛型类
     * @return list
     */
    private static <T> T[] convertJsonToArray(String json, Class<T> returnType) {
        final List<T> objectList = JSON.parseArray(json, returnType);
        final ArrayGeneric<T> arrayGeneric = new ArrayGeneric<>(returnType, objectList.size());
        for (T element : objectList) {
            arrayGeneric.add(element);
        }
        return arrayGeneric.getQueue();
    }
}