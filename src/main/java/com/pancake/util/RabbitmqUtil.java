package com.pancake.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.pojo.RabbitmqServer;
import com.pancake.entity.util.Const;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by chao on 2017/12/9.
 */

public class RabbitmqUtil {
    private final static Logger logger = LoggerFactory.getLogger(RabbitmqUtil.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private String queueName;
    private static ConnectionFactory factory = new ConnectionFactory();

//    static {
//
//        RabbitmqServer rabbitmqServer = JsonUtil.getRabbitmqServer(Const.BlockChainConfigFile);
//        factory.setUsername(rabbitmqServer.getUserName());
//        factory.setPassword(rabbitmqServer.getPassword());
////        factory.setVirtualHost(virtualHost);
//        factory.setHost(rabbitmqServer.getIp());
//        factory.setPort(rabbitmqServer.getPort());
//    }

    public RabbitmqUtil(String queueName) {
        this.init(queueName, "");
    }

    public RabbitmqUtil(String queueName, RabbitmqServer rabbitmqServer) {
        this.init(queueName, rabbitmqServer);
    }

    public RabbitmqUtil(String queueName, String blockChainConfigFile) {
        this.init(queueName, blockChainConfigFile);
    }

    public void init(String queueName, String blockChainConfigFile) {
        RabbitmqServer rabbitmqServer = null;
        if (blockChainConfigFile == null || blockChainConfigFile.equals("")) {
            rabbitmqServer = JsonUtil.getRabbitmqServer(Const.BlockChainConfigFile);
        } else {
            rabbitmqServer = JsonUtil.getRabbitmqServer(blockChainConfigFile);
        }
        init(queueName, rabbitmqServer);
    }

    public void init(String queueName, RabbitmqServer rabbitmqServer) {
        factory.setUsername(rabbitmqServer.getUserName());
        factory.setPassword(rabbitmqServer.getPassword());
//        factory.setVirtualHost(virtualHost);
        factory.setHost(rabbitmqServer.getIp());
        factory.setPort(rabbitmqServer.getPort());

        this.queueName = queueName;
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
            logger.debug("创建队列：" + this.queueName);
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getQueueName() {
        return this.queueName;
    }
    /**
     * 将 String push 到队列中
     *
     * @param message
     */
    public void push(String message) {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
            channel.basicPublish("", this.queueName, null, message.getBytes("UTF-8"));
            logger.debug(" [x] Sent '" + message + "'");
            channel.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将 list push 到队列中
     *
     * @param messages
     */
    public void push(List<String> messages) {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
//            channel.basicPublish("", this.queueName, null, messages.getBytes("UTF-8"));
            for (String message : messages) {
                channel.basicPublish("", this.queueName, null, message.getBytes("UTF-8"));
                logger.debug(" [x] Sent '" + message + "'");
            }
            channel.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从队列中获取一条消息
     */
    public String pull() {
        String content = null;
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            if(channel.messageCount(this.queueName) > 0) {
                GetResponse response = channel.basicGet(this.queueName, false);
                content = new String(response.getBody(), "UTF-8");
                logger.debug(content);
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            } else {
                logger.debug("队列为空");
            }
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }


    /**
     *
     * @param limitTime 单位：毫秒
     * @param limitSize 单位: MB
     * @return 接收到的消息 list
     */
    public List<String> pull(double limitTime, double limitSize) {
        List<String> msgList = new ArrayList<String>();
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            long totalLen = 0;
            long beginTime = System.nanoTime();
            //  1. 接收队列中消息的时间是否已超时
            while ((double) (System.nanoTime() - beginTime) / 1000000 < limitTime) {
                if(channel.messageCount(this.queueName) > 0) {
                    GetResponse response = channel.basicGet(this.queueName, false);
                    String msg = new String(response.getBody(), "UTF-8");
                    // 2. 接收队列中消息的大小是否超过限制
                    totalLen += msg.getBytes(Const.CHAR_SET).length;
                    if (totalLen / Math.pow(1024, 2) < limitSize) {
                        logger.debug("接收到队列消息：" + msg);
                        msgList.add(msg);
                        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

                    } else {
                        logger.info("大小超出限制，停止接收该消息，准备生成区块");
                        // requeue - true if the rejected message should be requeued rather than discarded/dead-lettered
                        channel.basicReject(response.getEnvelope().getDeliveryTag(), true);
                        break;
                    }
                }
                else {
                    logger.debug("队列为空");
                    break;
                }
            }
            logger.debug("接收结束");
            channel.close();
            conn.close();
            double size = totalLen / Math.pow(1024, 2);
            long timeUsage = (System.nanoTime() - beginTime) / 1000000;
            logger.info("获取待处理消息总大小为 [" + size + "] MB, 耗费时间为 [" + timeUsage + "], 处理速度为 ["
                    + size / timeUsage + "] MB/ms");
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msgList;
    }

}
