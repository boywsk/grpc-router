package com.gomeplus.rpc.router;

import com.gomeplus.rpc.util.ZKClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshikai on 2016/10/14.
 */
public class LoadBalanceRouter {
    private static Logger LOG = LoggerFactory.getLogger(LoadBalanceRouter.class);

    public static LoadBalanceRouter INSTANCE = new LoadBalanceRouter();

    public static ConcurrentHashMap<String, ManagedChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static ConcurrentSkipListSet<String> IP_PORT_LIST = new ConcurrentSkipListSet<>();

    private LoadBalanceRouter(){}

    public void init(final String zkIpPort, final String zkPath) {
        ZKClient.getInstance().getRpcZKPath(zkIpPort, zkPath);
    }

    public ManagedChannel getRouterChannel() {
        String ipPort = router();
        if (ipPort == null) {
            LOG.error("服务未能发现...，检查! router error");
            return null;
        }
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

    public ManagedChannel getRouterChannel(int timeoutSecs) {
        if (timeoutSecs <= 0 || timeoutSecs > 10) {
            timeoutSecs = 10;
        }
        String ipPort = router();
        if (ipPort == null) {
            LOG.error("服务未能发现...，检查! router error");
            return null;
        }
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ipPort.split(":")[0], Integer.parseInt(ipPort.split(":")[1])).usePlaintext(true).build();
        shutdown(channel, timeoutSecs);
        return channel;
    }

    private String router() {
        long seed = System.nanoTime();
        Object[] arr = IP_PORT_LIST.toArray();
        if (arr.length == 0) {
            return null;
        }
        long hashValue = seed % arr.length;
        return (String) arr[(int) hashValue];
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
