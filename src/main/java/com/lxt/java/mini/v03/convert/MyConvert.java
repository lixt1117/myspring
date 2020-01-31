package com.lxt.java.mini.v03.convert;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/31 20:14
 * @Description:自定义的针对httpRequest的请求参数类型转换接口
 */
public interface MyConvert<T> {

    /**
     * @Description:默认对参数不做转换
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/31 20:17
     */
    default T convert(String oldValue) {
        return (T) oldValue;
    }
}
