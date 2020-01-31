package com.lxt.java.mini.v03.convert;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/31 20:31
 * @Description:关于类型转换这块，本来想按老师写的策略模式搞个mapping来做的，然鹅上知乎
 * 发现有人提出一种枚举中定义方法的方式，我觉得很有趣，先尝试一下
 * ps：我之前都不知道枚举能定义方法。。。
 * 写完了发现，这种写法问题可能在于违反开闭原则了吧，不过就算策略模式，一点不修改也是不可能的，所以我觉得还好
 */
public enum MyConvertEnum {

    INTEGER {

        @Override
        public Object convert(String value) {
            return Integer.valueOf(value);
        }
    },
    DOUBLE() {

        @Override
        public Object convert(String value) {
            return Double.valueOf(value);
        }
    },
    STRING() {

        @Override
        public Object convert(String value) {
            return value;
        }
    };

    abstract public Object convert(String value);
}
