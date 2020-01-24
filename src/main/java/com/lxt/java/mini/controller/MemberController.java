package com.lxt.java.mini.controller;

import com.lxt.java.mini.annotation.*;
import com.lxt.java.mini.service.MemberService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/14 18:18
 * @Description:
 */
@MyController("/member")
public class MemberController {

    @MyAutoWired
    MemberService memberService;

    @MyRequestMapping("/getMemberInfoByName")
    public void getMemberInfoByName(HttpServletRequest request, HttpServletResponse response,
            @MyRequestParam("name") String name) throws IOException {
        try {
            String result = memberService.getMemberByName(name);
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("system error!");
        }
    }

    /**
     *
     * @Description:测试Method类获取参数类型和参数注解是不是按照参数声明的顺讯来的，经验证，返回数组是按照声明顺序排列
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/20 00:52
     */
    @MyRequestMapping("/testMethodGetParameterAnnotations")
    public void testMethodGetParameterAnnotations(HttpServletRequest request, HttpServletResponse response,
            @MyRequestParam2("name2") @MyRequestParam("name") String name, @MyRequestParam("age") String age) {
        try {
            String result = memberService.getMemberByName(name);
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/addTwoNum")
    public String addTwoNum(@MyRequestParam("a") Integer a, @MyRequestParam("b") Integer b) {
        return a + b + "";
    }
}
