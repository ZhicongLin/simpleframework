package org.simpleframework.http.proxy.visitor.exception;

/**
 * Description: 参数错误异常
 *
 * @author linzc
 * @version 1.0
 *
 * <pre>
 * 修改记录:
 * 修改后版本        修改人     修改日期        修改内容
 * 2020/7/2.1    linzc       2020/7/2     Create
 * </pre>
 * @date 2020/7/2
 */
public class RestParameterException extends RuntimeException {

    public RestParameterException(String message) {
        super(message);
    }
}