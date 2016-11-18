package com.gomeplus.rpc.global;


/**
 * Created by wangshikai on 2016/7/18.
 */
public class Global {

    /**
     * 请求类型
     */
    public static enum REQUEST_TYPE {

        REPORT(1),          // 汇报IM服务资源
        GET_RESOURCES(2),   // 获取IM服务资源

        RPC_REPORT(3),      //汇报RPC服务资源
        RPC_PULL(4);        //拉取RPC服务资源

        public int value;
        private REQUEST_TYPE(int value) {
            this.value = value;
        }
    }


    /**
     * 汇报的服务器类型
     */
    public static enum SERVER_TYPE {

        GATEWAY(1), // 接入
        LOGIC(2), // 逻辑
        API(3), // api
        FILE(4), // 文件
        ALL(5),  //全部
        RPC(6);  //RPC服务类型

        public int value;

        private SERVER_TYPE(int value) {
            this.value = value;
        }
    }

}
