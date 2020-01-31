package com.lxt.java.mini.service;

import com.lxt.java.mini.annotation.MyService;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/15 15:26
 * @Description:
 */
@MyService
public class MemberService {

    public String getMemberByName(String name, Integer age) throws Exception {
        return "name:" + name + "\n" + "age:" + age;
    }
}
