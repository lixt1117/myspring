package com.lxt.java.mini.annotation;

import java.lang.annotation.*;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/14 17:39
 * @Description:
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    String value() default "";
}
