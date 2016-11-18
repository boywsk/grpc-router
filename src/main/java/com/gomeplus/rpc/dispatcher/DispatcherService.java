package com.gomeplus.rpc.dispatcher;

import com.alibaba.fastjson.JSON;
import com.gomeplus.rpc.global.Global;
import com.gomeplus.rpc.model.request.ClientMsg;
import com.gomeplus.rpc.model.request.ReqRpcServersMsg;
import com.gomeplus.rpc.model.response.RpcServersMsg;
import com.gomeplus.rpc.model.response.RspRpcServersMsg;
import com.gomeplus.rpc.router.DispacherBalanceRouter;
import com.gomeplus.rpc.util.UdpUtil;
import com.gomeplus.rpc.util.ZKClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshikai on 2016/11/10.
 */
public class DispatcherService {
    private static Logger LOG = LoggerFactory.getLogger(DispatcherService.class);
    private final static int FIVE_MINUTES = 5 * 60 * 1000; //客户端汇报时间

    public static ConcurrentSkipListSet<String> DISPATCHER_SERVER_LIST = new ConcurrentSkipListSet<>();
    public static DispatcherService INSTANCE = new DispatcherService();

    private DispatcherService() {
    }

    /**
     * @param zkIpPort 调度服务注册的zk地址
     * @param zkPath   调度服务注册在zk中的路径
     */
    public void init(final String zkIpPort, final String zkPath) {
        ZKClient.getInstance().getZKPath(zkIpPort, zkPath, DISPATCHER_SERVER_LIST);
        //定期拉取rpc服务列表
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try{
                    String[] ipPortArr = getIpPort(DISPATCHER_SERVER_LIST);
                    ClientMsg clientMsg = createReqRpcServersMsg(Global.REQUEST_TYPE.RPC_PULL.value);
                    String receiveMsg = UdpUtil.SendMsg(ipPortArr[0], Integer.valueOf(ipPortArr[1]), JSON.toJSONString(clientMsg));
                    if(receiveMsg != null){
                        RspRpcServersMsg serversMsg = JSON.parseObject(receiveMsg, RspRpcServersMsg.class);
                        List<RpcServersMsg> rspServers = serversMsg.getRspServers();
                        Map<String, TreeMap<Double, String>> serverMap = new HashMap<>();
                        for (RpcServersMsg msg : rspServers) {
                            serverMap.put(msg.getCmd(), msg.getServers());
                        }
                        addServerMap(serverMap);
                    }else{
                        LOG.error("错误:请求不到调度服务的数据!请检查...");
                    }
                }catch (Exception e){
                    LOG.error("error:{}",e);
                }
            }
        }, 0, FIVE_MINUTES, TimeUnit.MILLISECONDS);
    }


    /**
     * 注意 ：同一命令字下的服务器ipPort和权重值均保证唯一,不重复   (权重值为调度服务计算下发的权重区间值不会重复,
     * 如： A B C D 四人的权重都为 1，那么下发的 A,B,C,D的权重值 为 1,2,3,4 ；
     * 即 A 的权重值区间在 （0,1];  B 的权重区间在(1,2] ; C 的权重区间在 (2,3] ; D 的权重区间在（3,,4];
     * 保证每人的权重概率范围均匀分布在最大权重值范围内)
     * <p>
     * <p>
     *
     * @param serverMap
     */
    public void addServerMap(Map<String, TreeMap<Double, String>> serverMap) {
        if (DispacherBalanceRouter.SERVER_MAP.isEmpty()) {
            Set<String> set = serverMap.keySet();
            for (String cmd : set) {
                TreeMap<Double, String> map = serverMap.get(cmd);
                Set<Double> weightSet = map.keySet();
                for (Double weight : weightSet) {
                    try {
                        String ipPort = map.get(weight);
                        String[] ip_port = ipPort.split(":");
                        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip_port[0], Integer.parseInt(ip_port[1])).usePlaintext(true).build();
                        if (!DispacherBalanceRouter.CHANNEL_MAP.containsKey(ipPort)) {
                            DispacherBalanceRouter.CHANNEL_MAP.put(ipPort, channel);
                        }
                    } catch (Exception e) {
                        LOG.error("create rpc channel error:{}", e);
                    }
                }
            }
        }else{
            DispacherBalanceRouter.SERVER_MAP.putAll(serverMap);
        }
    }

    private static String[] getIpPort(ConcurrentSkipListSet<String> DISPATCHER_SERVER_LIST) {
        long index = System.nanoTime() % DISPATCHER_SERVER_LIST.size();
        String ipPort = (String) DISPATCHER_SERVER_LIST.toArray()[(int) index];
        return ipPort.split(":");
    }

    //汇报服务资源消息
    public static ClientMsg createReqRpcServersMsg(int requestType) {
        ClientMsg clientMsg = new ClientMsg();
        clientMsg.setRequestType(requestType);
        ReqRpcServersMsg reqRpcServersMsg = new ReqRpcServersMsg();
        reqRpcServersMsg.setType(Global.SERVER_TYPE.RPC.value);
        clientMsg.setReqRpcServersMsg(reqRpcServersMsg);
        return clientMsg;
    }

}
