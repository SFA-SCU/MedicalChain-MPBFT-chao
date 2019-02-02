package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.message.*;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.NetService;
import com.pancake.util.MongoUtil;
import com.pancake.util.SignatureUtil;
import com.pancake.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;


/**
 * Created by chao on 2017/12/10.
 */
public class NewPrepareMessageService {
    private final static Logger logger = LoggerFactory.getLogger(NewPrepareMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理接收到的预准备消息
     *
     * @param receivedMessage
     * @param validatorAddress
     * @return
     * @throws IOException
     */
    public static boolean processPrepareMessage(String receivedMessage, NetAddress validatorAddress) throws IOException {
        String url = validatorAddress.toString();
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 PrepareMessage
        NewPrepareMessage prepareMessage = objectMapper.readValue(receivedMessage, NewPrepareMessage.class);
        logger.info("接收到 PrepareMsg：" + prepareMessage.getMsgId());
        logger.debug("开始校验 PrepareMsg ...");
        boolean verifyResult = NewPrepareMessageService.verify(prepareMessage);
        logger.debug("校验结束，结果为：" + verifyResult);

        if (verifyResult) {
            // 2. 校验结果为 true ，将 PrepareMessage 存入到集合中
            String pareMessageCollection = url + "." + Const.PM;
            if (NewPrepareMessageService.save(prepareMessage, pareMessageCollection)) {
                logger.debug("PrePrepareMessage [" + prepareMessage.getMsgId() + "] 已存入数据库");
            } else {
                logger.debug("PrePrepareMessage [" + prepareMessage.getMsgId() + "] 已存在");
            }

            // 3. 生成 CommitMessage，存入集合，并向其他节点进行广播
            NewCommitMessage commitMessage = NewCommitMessageService.genInstance(prepareMessage.getMsgId(),
                    validatorAddress.getIp(), validatorAddress.getPort());
            String commitMessageCollection = url + "." + Const.CMTM;
            NewCommitMessageService.save(commitMessage, commitMessageCollection);
            logger.debug("CommitMessage [" + commitMessage.getMsgId() + "] 已存入数据库");
            NetService.broadcastMsg(validatorAddress.getIp(), validatorAddress.getPort(), commitMessage.toString());
        }
        return verifyResult;
    }

    @SuppressWarnings("Duplicates")
    public static boolean save(NewPrepareMessage prepareMessage, String collectionName) {
        synchronized (NewPrepareMessageService.class) {
            if (MongoUtil.findByKV("msgId", prepareMessage.getMsgId(), collectionName)) {
                logger.error("prepareMessage: [" + prepareMessage.getMsgId() + "] 已存在");
                return false;
            } else {
                MongoUtil.insertJson(prepareMessage.toString(), collectionName);
                return true;
            }
        }
    }

    public static boolean save(String prepareMessageStr, String collectionName) {
        NewPrepareMessage prepareMessage = null;
        try {
            prepareMessage = objectMapper.readValue(prepareMessageStr, NewPrepareMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(prepareMessage, collectionName);
    }

    /**
     * 根据 序列号 与 clientMsg 对象生成预准备消息对象
     * @param clientMsg
     * @return
     */
    public static NewPrepareMessage genInstance(ClientMessage clientMsg) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(clientMsg.getMsgId(), timestamp));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new NewPrepareMessage(msgId, timestamp, pubKey, signature, clientMsg);
    }

    /**
     * 检验 PrepareMessage的正确性
     *
     * @param prepareMessage
     * @return
     */
    public static boolean verify(NewPrepareMessage prepareMessage) {
        return SignatureUtil.verify(prepareMessage.getPubKey(), getSignContent(prepareMessage.getClientMsg().getMsgId(),
                prepareMessage.getTimestamp()), prepareMessage.getSignature());
    }

    /**
     * 根据传入的内容生成 prepareMessage 要签名的字符串
     *
     * @param clientMsgId
     * @param timestamp
     * @return
     */
    public static String getSignContent(String clientMsgId, String timestamp) {
        StringBuilder sb = new StringBuilder();
        sb.append(clientMsgId).append(timestamp);
        return sb.toString();
    }
}
