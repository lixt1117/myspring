package com.lxt.java.mini.util;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/16 15:58
 * @Description:字符串工具类
 */
public class StringUtils {

    static String EMPTY = "";

    /**
     * @Description:字符串判空：null、""、全空格都认为是空
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/16 16:00
     */
    public static boolean isEmpty(String str) throws Exception {
        if (null == str || str.trim().equals(EMPTY)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @Description:字符串首字母小写——利用大小写字母的ascii码相差32位实现
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/23 22:58
     */
    public static String toLowercaseFirstLetter(String str) throws Exception {
        char[] chars = str.toCharArray();
        // 这个范围代表A～Z
        if (chars[0] >= 65 && chars[0] <= 90) {
            chars[0] += 32;
            return new String(chars);
        }
        return str;
    }
}
