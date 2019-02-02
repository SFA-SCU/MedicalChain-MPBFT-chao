package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.message.*;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.MongoUtil;
import com.pancake.util.SignatureUtil;
import com.pancake.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;


/**
 * Created by chao on 2017/11/21.
 */
public class NewCommitMessageService {
    private final static Logger logger = LoggerFactory.getLogger(NewCommitMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理接收到的 commit message
     *
     * @param receivedMessage
     * @param validatorAddr
     * @throws IOException
     */
    @SuppressWarnings("Duplicates")
    public void processCommitMessage(String receivedMessage, NetAddress validatorAddr) throws IOException {
        String url = validatorAddr.toString();
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 CommitMessage
        NewCommitMessage commitMessage = objectMapper.readValue(receivedMessage, NewCommitMessage.class);
        logger.info("接收到 CommitMsg：" + receivedMessage);
        logger.debug("开始校验 CommitMsg ...");
        boolean verifyResult = NewCommitMessageService.verify(commitMessage);
        logger.debug("校验结束，结果为：" + verifyResult);

        if (verifyResult) {
            String commitMessageCollection = url + "." + Const.CMTM;
            String prepareMessageCollection = url + "." + Const.PM;

            NewPrepareMessage prepareMessage = MongoUtil.findPrepareMessageById(commitMessage.getPrepareMessageId(),
                    prepareMessageCollection);
            if (prepareMessage != null) {
                // 1. 统计 PrepareMessageId 出现的次数
//                int count = MongoUtil.countPPMSign(commitMessage.getPrepareMessageId(), commitMessageCollection);
                int count = MongoUtil.countPrepareMessageId(commitMessage.getPrepareMessageId(), commitMessageCollection);
                logger.debug("count = " + count);

                // 2. 将 CommitMessage 存入到集合中
                if (NewCommitMessageService.save(commitMessage, commitMessageCollection)) {
                    logger.debug("将CommitMessage [" + commitMessage.getMsgId() + "] 存入数据库");
                } else {
                    logger.debug("CommitMessage [" + commitMessage.getMsgId() + "] 已存在");
                }

                // 3. 达成 count >= 2 * f 后存入到集合中
//                if (2 * PeerUtil.getFaultCount() <= count) {
//                    CommittedMessage cmtdm = cmtdmService.genInstance(prepareMessage.getClientMsg().getMsgId(), prepareMessage.getViewId(),
//                            prepareMessage.getSeqNum(), validatorAddr.getIp(), validatorAddr.getPort());
//                    ClientMessage clientMessage = prepareMessage.getClientMsg();
//                    cmtdmService.procCMTDM(cmtdm, clientMessage, validatorAddr);
//                }
                //TODO
            }
        }

    }

    public static NewCommitMessage genInstance(String prepareMessageId, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(prepareMessageId, timestamp, ip, port));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new NewCommitMessage(msgId, timestamp, pubKey, signature, prepareMessageId, ip, port);

    }

    /**
     * 生成签名的内容
     * @param prepareMessageId
     * @param timestamp
     * @param ip
     * @param port
     * @return
     */
    public static String getSignContent(String prepareMessageId, String timestamp, String ip, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append(prepareMessageId).append(timestamp).append(ip).append(port);
        return sb.toString();
    }

    /**
     *
     * @param commitMessage
     * @param collectionName
     * @return
     */
    public static boolean save(NewCommitMessage commitMessage, String collectionName) {
        synchronized (NewCommitMessageService.class) {
            if (MongoUtil.findByKV("msgId", commitMessage.getMsgId(), collectionName)) {
                return false;
            } else {
                MongoUtil.insertJson(commitMessage.toString(), collectionName);
                return true;
            }
        }
    }

    /**
     *
     * @param commitMessage
     * @return
     */
    public static boolean verify(NewCommitMessage commitMessage) {
        return SignatureUtil.verify(commitMessage.getPubKey(), getSignContent(commitMessage.getPrepareMessageId(),
                commitMessage.getTimestamp(), commitMessage.getIp(), commitMessage.getPort()),
                commitMessage.getSignature());
    }
}
