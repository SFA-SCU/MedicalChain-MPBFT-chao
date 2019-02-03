package com.pancake.service.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

/**
 * Created by chao on 2017/12/18.
 */
public class NewNetService {
    private final static Logger logger = LoggerFactory.getLogger(NewNetService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static List<NetAddress> validatorAddressList =
            JsonUtil.getValidatorAddressList(Const.BlockChainConfigFile);

    private static class LazyHolder {
        private static final NewNetService INSTANCE = new NewNetService();
    }

    private NewNetService() {
    }

    public static NewNetService getInstance() {
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
    public void broadcastMsg(String ip, int localPort, String msg) throws IOException {
//        List<NetAddress> list = JsonUtil.getValidatorAddressList(Const.BlockChainConfigFile);
        for (NetAddress validatorAddres : validatorAddressList) {
            // 排除本机，向 BlockChainConfigFile 中存储的其他验证节点发送预准备消息
            if (!((validatorAddres.getIp().equals(ip) || validatorAddres.getIp().equals("127.0.0.1"))
                    && validatorAddres.getPort() == localPort)) {
                Socket broadcastSocket = new Socket(validatorAddres.getIp(), validatorAddres.getPort());
                OutputStream outToServer = broadcastSocket.getOutputStream();
                DataOutputStream outputStream = new DataOutputStream(outToServer);
                logger.info("开始向 " + validatorAddres.getIp() + ":" + validatorAddres.getPort() + " 发送消息: " + msg);
                outputStream.writeUTF(msg);

                InputStream inFromServer = broadcastSocket.getInputStream();
                DataInputStream inputStream = new DataInputStream(inFromServer);
                String rcvMsg = inputStream.readUTF();
                logger.info("服务器响应消息的结果为： " + rcvMsg);

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
        logger.info("开始发送 msg: " + msg);
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
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(msg);
            out.flush();
            logger.info("完成写入");

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            rcvMsg = in.readUTF();
            logger.info("服务器响应： " + rcvMsg);
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
