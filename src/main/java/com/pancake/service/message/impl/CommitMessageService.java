package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.CommitMessageCount;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.message.*;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.NetService;
import com.pancake.service.component.TransactionService;
import com.pancake.util.MongoUtil;
import com.pancake.util.PeerUtil;
import com.pancake.util.SignatureUtil;
import com.pancake.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;


/**
 * Created by chao on 2017/11/21.
 */
public class CommitMessageService {
    private final static Logger logger = LoggerFactory.getLogger(CommitMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static CommitMessageService commitMessageService = CommitMessageService.getInstance();
    private final static PrepareMessageService prepareMessageService = PrepareMessageService.getInstance();
    private final static TransactionService txService = TransactionService.getInstance();

    private static class LazyHolder {
        private static final CommitMessageService INSTANCE = new CommitMessageService();
    }
    private CommitMessageService() {
    }
    public static CommitMessageService getInstance() {
        return CommitMessageService.LazyHolder.INSTANCE;
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
        CommitMessage commitMessage = objectMapper.readValue(receivedMessage, CommitMessage.class);
        logger.info("接收到来自" + commitMessage.getIp() + ":" + commitMessage.getPort() +
                "CommitMsg：" + commitMessage.getMsgId());
        logger.debug("开始校验 CommitMsg ...");
        boolean verifyResult = this.verify(commitMessage);
        logger.debug("校验结束，结果为：" + verifyResult);

        if (verifyResult) {
            String commitMessageCollection = url + "." + Const.CMTM;
            String prepareMessageCollection = url + "." + Const.PM;
//            String commitMessageCountCollection = url + "." + Const.CMTM_COUNT;

            PrepareMessage prepareMessage = MongoUtil.findPrepareMessageById(commitMessage.getPrepareMessageId(),
                    prepareMessageCollection);

//            if (prepareMessage != null) {
//                // 1. 统计 PrepareMessageId 出现的次数
////                int count = MongoUtil.countPPMSign(commitMessage.getPrepareMessageId(), commitMessageCollection);
//                int count = MongoUtil.countPrepareMessageId(commitMessage.getPrepareMessageId(), commitMessageCollection);
//                logger.debug("count = " + count);

                // 1. 将 CommitMessage 存入到集合中
                if (this.save(commitMessage, commitMessageCollection)) {
                    logger.info("将CommitMessage [" + commitMessage.getMsgId() + "] 存入数据库");
                    // 2. 更新某交易单所对应的来自不同节点的 commit message 的个数
                    String clientMsgId = commitMessage.getClientMsgId();
//                    boolean result = this.updateCommitMessageQuantity(clientMsgId, url);
                    boolean result = this.updateCommitMessageQuantity(commitMessage, validatorAddr);
                    if (result) {
                        logger.debug("更新 " + clientMsgId + "数量成功");
                    } else {
                        logger.error("更新 " + clientMsgId + "数量失败");
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
//            }
        }

    }

    public CommitMessage genInstance(String prepareMessageId, String clientMsgId, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(prepareMessageId, timestamp, ip, port));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new CommitMessage(msgId, timestamp, pubKey, signature, prepareMessageId, clientMsgId, ip, port);

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
    public boolean save(CommitMessage commitMessage, String collectionName) {
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
    public boolean verify(CommitMessage commitMessage) {
        return SignatureUtil.verify(commitMessage.getPubKey(), getSignContent(commitMessage.getPrepareMessageId(),
                commitMessage.getTimestamp(), commitMessage.getIp(), commitMessage.getPort()),
                commitMessage.getSignature());
    }

    public boolean updateCommitMessageQuantity(CommitMessage commitMessage, NetAddress validatorAddress){
        String url = validatorAddress.toString();
        String commitMessageCountCollection = url + "." + Const.CMTM_COUNT;
        String txMsgCollection = url + "." + Const.TXM;
        String prepareMsgCollection = url + "." + Const.PM;
        String txCollection = url + "." + Const.TX;
        String clientMsgId = commitMessage.getClientMsgId();
        synchronized (this) {
            String record = MongoUtil.findOne("clientMsgId", clientMsgId, commitMessageCountCollection);
            CommitMessageCount commitMessageCount = null;
            if (null != record && !record.equals("") ) {
                try {
                    commitMessageCount = objectMapper.readValue(record, CommitMessageCount.class);
                    int clientMsgIdCount = commitMessageCount.getClientMsgIdCount() + 1;
                    // 数量加1
                    MongoUtil.update("clientMsgId", clientMsgId, "clientMsgIdCount", clientMsgIdCount,
                            commitMessageCountCollection);
                    logger.debug("clientMsgId: " + clientMsgId);

                    // 若满足2f，则提交Client
                    if (clientMsgIdCount >= 2 * PeerUtil.getFaultCount() && !commitMessageCount.isCommitted()) {
                        // 1. 检查当前节点是否为主节点
                        if(MongoUtil.findByKV("msgId", clientMsgId, txMsgCollection)) {
                            // 2. 生成 CommitMessage，存入集合，并向其他节点进行广播
                            CommitMessage newCommitMessage = commitMessageService.genInstance(
                                    commitMessage.getPrepareMessageId(), clientMsgId, validatorAddress.getIp(),
                                    validatorAddress.getPort());
                            String commitMessageCollection = url + "." + Const.CMTM;
                            commitMessageService.save(newCommitMessage , commitMessageCollection);
                            logger.debug("CommitMessage [" + newCommitMessage .getMsgId() + "] 已存入数据库");
                            NetService.broadcastMsg(validatorAddress.getIp(), validatorAddress.getPort(),
                                    newCommitMessage.toString());
                        }
                        // 3. 正式提交来自客户端的提案
                        PrepareMessage prepareMessage = prepareMessageService.getByClientMsgId(clientMsgId,
                                prepareMsgCollection);
                        ClientMessage clientMsg = prepareMessage.getClientMsg();
//                        String cliMsgType = clientMsg.getMsgType();
                        String cliMsgType = clientMsg.getClass().getSimpleName();
                        if (cliMsgType.equals(TransactionMessage.class.getSimpleName())) {
                            // 如果 clientMessage 引用的对象为 TransactionMessage 类型
                            TransactionMessage txMessage = (TransactionMessage) clientMsg;
                            List<Transaction> txList = txMessage.getTxList();
                            if (txService.saveBatch(txList, txCollection)) {
                                List<String> txIdList = txService.getTxIdList(txList);
//                                logger.info("交易 :" + txIdList + " 存入成功");
                                logger.info("交易 :" + txIdList.get(0) + " 等存入成功");

                                //TODO 验证成功的 tx 发送到 blocker 服务器上
//                                TxIdMessage txIdMsg = timSrv.genInstance(txIdList, netAddress.getIp(), netAddress.getPort());
//                                netService.sendMsg(txIdMsg.toString(), blockerAddr.getIp(), blockerAddr.getPort());
                            }
                        } else {
                            logger.error("未知ClientMsg类型: " + cliMsgType);
                        }

                        // 更新 CommitMessageCount 的 committed 状态为 true
                        MongoUtil.update("clientMsgId", clientMsgId, "committed", true,
                                commitMessageCountCollection);

                    }

                } catch (IOException e) {
                    logger.error("commitMessageCount [" + record + "] 解析失败， 错误信息为： " + e.getMessage());
                    return false;
                }

            } else {
                commitMessageCount = new CommitMessageCount(clientMsgId, 1, false);
                MongoUtil.insertJson(commitMessageCount.toString(), commitMessageCountCollection);
            }
            return true;
        }
    }
}
