package com.lxt.java.mini.exception;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/20 11:04
 * @Description:
 */
public class HttpException extends Exception {

    private Enum httpCode;

    public HttpException(Enum httpCode) {
        this.httpCode = httpCode;
    }

    public Enum getHttpCode() {
        return httpCode;
    }
}
