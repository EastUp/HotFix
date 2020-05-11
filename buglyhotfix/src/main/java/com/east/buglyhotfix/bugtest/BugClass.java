package com.east.buglyhotfix.bugtest;

/**
 * 测试bug类.
 *
 * @author devilwwj
 * @since 2016/10/25
 */
public class BugClass {

    public String bug() {
        // 这段代码会报空指针异常
        String str = null;
        //Log.e("BugClass", "get String length:" + str.length());
//        return "This is a bug class";
        return "This is a fixed bug class";
    }
}
