package com.gomeplus.rpc.router;

import com.gomeplus.rpc.dispatcher.DispatcherService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshikai on 2016/11/14.
 */
public class DispacherBalanceRouter {
    private static Logger LOG = LoggerFactory.getLogger(DispacherBalanceRouter.class);

    public static DispacherBalanceRouter INSTANCE = new DispacherBalanceRouter();
    /**
     * map<cmd,treemap<权重区间值,serverIpPort>>
     */
    public static ConcurrentHashMap<String, TreeMap<Double, String>> SERVER_MAP = new ConcurrentHashMap<>();

    /**
     * map<serverIpPort,channel>
     */
    public static ConcurrentHashMap<String, ManagedChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private DispacherBalanceRouter() {}

    public void init(String zkIpPort, String zkPath){
        DispatcherService.INSTANCE.init(zkIpPort,zkPath);
    }

    public ManagedChannel getRouterChannel(String rpcServerCmd) {
        TreeMap<Double, String> treeMap = SERVER_MAP.get(rpcServerCmd);
        double bingo = router(treeMap);
        String ipPort = treeMap.get(bingo);
        LOG.info("bingo:{},ipPort:{}",bingo,ipPort);
        ManagedChannel channel = CHANNEL_MAP.get(ipPort);
        if (channel == null || channel.isShutdown() || channel.isTerminated()) {
            try {
                channel = ManagedChannelBuilder.forAddress(ipPort.split(":")[0], Integer.parseInt(ipPort.split(":")[1])).usePlaintext(true).build();
                CHANNEL_MAP.put(ipPort, channel);
            } catch (Exception e) {
                CHANNEL_MAP.remove(ipPort);
                LOG.error("create channel error:{}", e);
            }
        }
        return channel;
    }

    private double router(TreeMap<Double, String> treeMap) {
        double seed = treeMap.lastKey() * Math.random();
        double bingo = treeMap.tailMap(seed, false).firstKey();
        return bingo;
    }

    public void shutdown(ManagedChannel channel, int timeoutSecs) {
        if (channel == null) {
            LOG.error("channel is null");
            return;
        }
        try {
            channel.shutdown().awaitTermination(timeoutSecs, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("channel shutdown error exception", e);
        }
    }


}
