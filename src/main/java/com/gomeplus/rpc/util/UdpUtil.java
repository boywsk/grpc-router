package com.gomeplus.rpc.util;

import com.gomeplus.rpc.callback.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by wangshikai on 2016/11/9.
 */
public class UdpUtil {

    private static Logger LOG = LoggerFactory.getLogger(UdpUtil.class);

    /**
     * 接收UDP组播消息
     *
     * @param MultiIp 组播服务器地址  D类网段 "224.0.0.5"
     * @param port    组播端口
     */
    public static void multiServer(String MultiIp, int port,Callback callback) {
        try {
            //组播服务器地址
            //InetAddress address = InetAddress.getByName("224.0.0.5");
            //组播端口
            //int port = 6666;
            InetAddress address = InetAddress.getByName(MultiIp);
            MulticastSocket server = new MulticastSocket(port);
            server.joinGroup(address);

            //接收服务器反馈数据
            byte[] buf = new byte[2048];
            DatagramPacket backPacket = new DatagramPacket(buf, buf.length);
            while (true) {
                server.receive(backPacket);  //接收返回数据
                String receiveMsg = new String(buf, 0, backPacket.getLength());
                LOG.info("接收到组播数据为:" + receiveMsg);
                callback.call(receiveMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送UDP组播消息
     *
     * @param MultiIp
     * @param port
     * @param msg
     */
    public static void multiUdpSend(String MultiIp, int port, String msg) {
        try {
            //组播服务器地址
            //InetAddress address = InetAddress.getByName("224.0.0.5");
            //int port = 6666;
            InetAddress address = InetAddress.getByName(MultiIp);

            MulticastSocket client = new MulticastSocket(port);

            DatagramPacket dataGramPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);

            client.send(dataGramPacket);

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param ip
     * @param port
     * @param msg
     * @return
     */
    public static String SendMsg(String ip, int port, String msg) {
        String receiveMsg = null;
        try {
            if (msg == null) {
                LOG.error("msg:{} is empty!", msg);
                return null;
            }
            InetAddress address = InetAddress.getByName(ip);
            //InetAddress address = InetAddress.getByName("10.125.72.89");  //服务器地址  10.125.3.61   10.69.16.92
            //int port = 8866;  //服务器的端口号  国美+ 8877   企业办公 8866
            //创建发送方的数据报信息
            DatagramPacket dataGramPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);

            DatagramSocket socket = new DatagramSocket();  //创建套接字
            socket.send(dataGramPacket);  //通过套接字发送数据
            socket.setSoTimeout(5000);

            //接收服务器反馈数据
            byte[] buf = new byte[2048];
            DatagramPacket backPacket = new DatagramPacket(buf, buf.length);
            socket.receive(backPacket);  //接收返回数据
            receiveMsg = new String(buf, 0, backPacket.getLength());
            LOG.info("UDP服务器返回的数据为:" + receiveMsg);
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
            LOG.error("error:{}", e);
        }
        return receiveMsg;
    }


//    public static void main(String[] args) {
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                multiServer("224.0.0.5", 6666, new Callback() {
//                    @Override
//                    public void call(String msg) {
//                        System.out.println("回调处理返回数据:"+msg);
//                    }
//                });
//            }
//        });
//        t.start();
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        multiUdpSend("224.0.0.5", 6666, "{\"requestType\": 2,\"reqServersMsg\": {\"type\": 2}}");
//        UdpUtil.SendMsg("224.0.0.5", 6666,"{\"requestType\": 2,\"reqServersMsg\": {\"type\": 2}}");
//    }

    public static void main(String[] args) {
        String msg = UdpUtil.SendMsg("10.125.3.61",8866,"{\"requestType\": 2,\"reqServersMsg\": {\"type\": 2}}");
        System.out.println("结果:"+msg);
    }
}
