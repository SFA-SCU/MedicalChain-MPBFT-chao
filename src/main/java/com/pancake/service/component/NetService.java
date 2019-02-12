package com.pancake.service.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.JsonUtil;
import com.pancake.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pancake.entity.util.Const;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

/**
 * Created by chao on 2017/12/18.
 */
public class NetService {
    private final static Logger logger = LoggerFactory.getLogger(NetService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private static class LazyHolder {
        private static final NetService INSTANCE = new NetService();
    }

    private NetService() {
    }

    public static NetService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 向除了 ip:localport 以外的 url 地址广播消息 msg
     *
     * @param ip
     * @param localPort
     * @param msg
     * @throws IOException
     */
    public static void broadcastMsg(String ip, int localPort, String msg) throws IOException {
        List<NetAddress> list = JsonUtil.getValidatorAddressList(Const.BlockChainConfigFile);
        for (NetAddress va : list) {
            // 排除本机，向 BlockChainConfigFile 中存储的其他节点发送预准备消息
            if (!((va.getIp().equals(ip) || va.getIp().equals("127.0.0.1")) && va.getPort() == localPort)) {
                Socket broadcastSocket = new Socket(va.getIp(), va.getPort());
                OutputStream outToServer = broadcastSocket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outToServer);
//                logger.info("开始向 " + va.getIp() + ":" + va.getPort() + " 发送消息: " + msg);
                logger.info("开始向 " + va.getIp() + ":" + va.getPort() + " 发送消息: " + msg.split("\"")[3]);
//                dataOutputStream.writeUTF(msg);
                NetUtil.write(dataOutputStream, msg);
                broadcastSocket.shutdownOutput();

                InputStream inFromServer = broadcastSocket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inFromServer);
//                String rcvMsg = dataInputStream.readUTF();
                String rcvMsg = NetUtil.read(dataInputStream);
                logger.info("服务器响应消息的结果为： " + rcvMsg);

                dataInputStream.close();
                dataOutputStream.close();
                inFromServer.close();
                outToServer.close();
                broadcastSocket.close();
            }
        }
    }

    /**
     * 向指定 url 发送消息 msg
     *
     * @param msg
     * @param ip
     * @param port
     * @param timeout 连接超时时间
     */
    public String sendMsg(String msg, String ip, int port, int timeout) {
        String rcvMsg = null;
        logger.info("开始发送 msg: " + msg.split("\"")[3]);
        Socket client = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        try {
            if (timeout != -1) {
                client.connect(socketAddress, timeout);
            } else {
                client.connect(socketAddress);
            }
            logger.info("连接到主机：" + ip + " ，端口号：" + port);
        } catch (ConnectException e) {
            logger.error("连接主机：" + ip + " ，端口号：" + port + " 拒绝！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outToServer);
            NetUtil.write(dataOutputStream, msg);
            client.shutdownOutput();
            logger.debug("完成写入");

            InputStream inFromServer = client.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inFromServer);
//            rcvMsg = in.readUTF();
            rcvMsg = NetUtil.read(dataInputStream);
            // 关闭资源
            dataInputStream.close();
            dataOutputStream.close();
            inFromServer.close();
            outToServer.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rcvMsg;
    }

    public String sendMsg(String msg, String ip, int port) {
        return sendMsg(msg, ip, port, -1);
    }

}
