package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.CommitMessageCount;
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

    private static class LazyHolder {
        private static final NewCommitMessageService INSTANCE = new NewCommitMessageService();
    }
    private NewCommitMessageService() {
    }
    public static NewCommitMessageService getInstance() {
        return NewCommitMessageService.LazyHolder.INSTANCE;
    }

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
        logger.debug("本机地址为：" + url);

        // 1. 校验接收到的 CommitMessage
        NewCommitMessage commitMessage = objectMapper.readValue(receivedMessage, NewCommitMessage.class);
        logger.info("接收到 CommitMsg：" + receivedMessage);
        logger.debug("开始校验 CommitMsg ...");
        boolean verifyResult = this.verify(commitMessage);
        logger.debug("校验结束，结果为：" + verifyResult);

        if (verifyResult) {
            String commitMessageCollection = url + "." + Const.CMTM;
            String prepareMessageCollection = url + "." + Const.PM;
            String commitMessageCountCollection = url + "." + Const.CMTM_COUNT;

            PrepareMessage prepareMessage = MongoUtil.findPrepareMessageById(commitMessage.getPrepareMessageId(),
                    prepareMessageCollection);
            if (prepareMessage != null) {
//                // 1. 统计 PrepareMessageId 出现的次数
////                int count = MongoUtil.countPPMSign(commitMessage.getPrepareMessageId(), commitMessageCollection);
//                int count = MongoUtil.countPrepareMessageId(commitMessage.getPrepareMessageId(), commitMessageCollection);
//                logger.debug("count = " + count);

                // 1. 将 CommitMessage 存入到集合中
                if (this.save(commitMessage, commitMessageCollection)) {
                    logger.debug("将CommitMessage [" + commitMessage.getMsgId() + "] 存入数据库");
                    // 2. 更新某交易单所对应的来自不同节点的 commit message 的个数
                    boolean result = this.updateCommitMessageQuantity(commitMessage.getTxId(),
                            commitMessageCountCollection);
                    if (result) {
                        logger.debug("更新 " + commitMessage.getTxId() + "数量成功");
                    } else {
                        logger.debug("更新 " + commitMessage.getTxId() + "数量失败");
                    }

                } else {
                    logger.error("CommitMessage [" + commitMessage.getMsgId() + "] 已存在");
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

    public NewCommitMessage genInstance(String prepareMessageId, String txId, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(prepareMessageId, timestamp, ip, port));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new NewCommitMessage(msgId, timestamp, pubKey, signature, prepareMessageId, txId, ip, port);

    }

    /**
     * 生成签名的内容
     * @param prepareMessageId
     * @param timestamp
     * @param ip
     * @param port
     * @return
     */
    public String getSignContent(String prepareMessageId, String timestamp, String ip, int port) {
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
    public boolean save(NewCommitMessage commitMessage, String collectionName) {
        synchronized (this) {
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
    public boolean verify(NewCommitMessage commitMessage) {
        return SignatureUtil.verify(commitMessage.getPubKey(), getSignContent(commitMessage.getPrepareMessageId(),
                commitMessage.getTimestamp(), commitMessage.getIp(), commitMessage.getPort()),
                commitMessage.getSignature());
    }

    public boolean updateCommitMessageQuantity(String txId, String collectionName){
        synchronized (this) {
            String record = MongoUtil.findOne("txId", txId, collectionName);
            CommitMessageCount commitMessageCount = null;
            if (null != record && !record.equals("") ) {
                try {
                    commitMessageCount = objectMapper.readValue(record, CommitMessageCount.class);
                    int txIdCount = commitMessageCount.getTxIdCount() + 1;
                    // 数量加1
                    MongoUtil.update("txId", txId, "txIdCount", txIdCount,
                            collectionName);
                    logger.debug("txId: " + txId);
                    // TODO 若满足2f，则提交Tx

                } catch (IOException e) {
                    logger.error("commitMessageCount [" + record + "] 解析失败， 错误信息为： " + e.getMessage());
                    return false;
                }

            } else {
                commitMessageCount = new CommitMessageCount(txId, 1, false);
                MongoUtil.insertJson(commitMessageCount.toString(), collectionName);
            }
            return true;
        }
    }
}
