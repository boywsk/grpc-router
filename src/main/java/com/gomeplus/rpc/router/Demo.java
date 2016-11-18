package com.gomeplus.rpc.router;

/**
 * Created by wangshikai on 2016/11/14.
 */
public class Demo {
    public static void main(String[] args) {
        //DispacherBalanceRouter 初始化
        DispacherBalanceRouter.INSTANCE.init("10.125.3.31:2181", "/im-dispatcher");

        DispacherBalanceRouter.INSTANCE.getRouterChannel("GroupMemberMarkSericeGrpc");
    }
}
