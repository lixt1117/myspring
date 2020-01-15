package com.lxt.java.mini.annotation;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/14 17:44
 * @Description:
 */

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {
    String value() default "";
}
