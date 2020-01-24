package com.lxt.java.mini.v02;

import java.lang.reflect.Method;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/19 15:23
 * @Description:handlerMapping里保存的method实例，主要包含Method实例和参数名称
 */
public class MyMethod {

    private Method method;

    private String[] paramNames;

    private String simpleClazzName;

    public MyMethod(Method method, String[] paramNames,
                    String simpleClazzName) {
        this.method = method;
        this.paramNames = paramNames;
        this.simpleClazzName = simpleClazzName;
    }

    public Method getMethod() {
        return method;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public String getSimpleClazzName() {
        return simpleClazzName;
    }
}
