package com.lxt.java.mini.controller;

import com.lxt.java.mini.annotation.MyAutoWired;
import com.lxt.java.mini.annotation.MyController;
import com.lxt.java.mini.annotation.MyRequestMapping;
import com.lxt.java.mini.annotation.MyRequestParam;
import com.lxt.java.mini.service.MemberService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public void getMemberInfoByName(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @MyRequestParam("name") String name) {
        try {
            String result = memberService.getMemberByName(name);
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/addTwoNum")
    public String addTwoNum(@MyRequestParam("a") Integer a, @MyRequestParam(
            "b") Integer b) {
        return a + b + "";
    }

}
