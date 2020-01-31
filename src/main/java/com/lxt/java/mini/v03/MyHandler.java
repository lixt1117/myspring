package com.lxt.java.mini.v03;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/31 20:06
 * @Description:handlerMapping里保存的handler实例，包含method实例、controller实例、url的正则、方法的参数名称数组
 * 这里之所以还保留我的v2版本写法，没有和老师一样保存参数的索引顺序数组，是因为我对比之后觉得两种写法原理基本一样，
 * 且性能上应该不会有什么差异
 */
public class MyHandler {

    private Pattern urlPattern;

    private Method method;

    private String[] paramNames;

    private Object controller;

    public MyHandler(Pattern urlPattern, Method method, String[] paramNames, Object controller) {
        this.urlPattern = urlPattern;
        this.method = method;
        this.paramNames = paramNames;
        this.controller = controller;
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }

    public Method getMethod() {
        return method;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public Object getController() {
        return controller;
    }
}
