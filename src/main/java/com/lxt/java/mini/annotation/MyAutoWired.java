package com.lxt.java.mini.annotation;

import java.lang.annotation.*;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/14 17:46
 * @Description:
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutoWired {
    boolean required() default true;

    String value() default "";
}
