package com.lxt.java.mini.annotation;

import java.lang.annotation.*;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/14 17:47
 * @Description:
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    String value() default "";

    String name() default "";

//    String[] value() default {};

    String[] path() default {};

    String[] params() default {};

    String[] headers() default {};

    String[] consumes() default {};

    String[] produces() default {};
}
