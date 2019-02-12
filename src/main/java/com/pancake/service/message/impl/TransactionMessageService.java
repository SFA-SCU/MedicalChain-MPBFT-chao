package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.dao.TransactionDao;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.message.PrepareMessage;
import com.pancake.entity.message.TransactionMessage;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.NetService;
import com.pancake.util.MongoUtil;
import com.pancake.util.RabbitmqUtil;
import com.pancake.util.SignatureUtil;
import com.pancake.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;


/**
 * Created by chao on 2017/12/18.
 */
public class TransactionMessageService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private TransactionDao txDao = TransactionDao.getInstance();
    private NetService netService = NetService.getInstance();
    private PrepareMessageService prepareMessageService = PrepareMessageService.getInstance();

    private static class LazyHolder {
        private static final TransactionMessageService INSTANCE = new TransactionMessageService();
    }
    private TransactionMessageService() {
    }
    public static TransactionMessageService getInstance() {
        return TransactionMessageService.LazyHolder.INSTANCE;
    }

    /**
     * 接收到 ClientMessage 为 TransactionMessage 时进行的一系列处理
     *
     * @param rcvMsg
     * @param validatorAddress
     */
    public void processTxMessage(String rcvMsg, NetAddress validatorAddress) {
        String url = validatorAddress.toString();
        TransactionMessage txMsg = null;
        try {
            txMsg = objectMapper.readValue(rcvMsg, TransactionMessage.class);
        } catch (IOException e) {
            logger.error("txMsg 解析失败，错误消息为：" + e.getMessage());
        }

        // 校验 Tx Msg
        if (txMsg != null && verify(txMsg)) {
            // 1. Tx Message 存入到集合中
//            String txCollection = url + "." + Const.TXM;
            String txCollection = url + "." + Const.TXM;
            if (this.save(txMsg, txCollection)) {
                logger.info("Tx Message: " + txMsg.getMsgId() + " 存入成功");
            } else {
                logger.error("Tx Message: " + txMsg.getMsgId() + " 已存在");
            }

            // 2. 根据 Tx Message 生成 PrepareMessage，存入到集合中
            String prepareMessageCollection = url + "." + Const.PM;
//            String pmCollection = Const.PM;

            PrepareMessage prepareMessage = prepareMessageService.genInstance(txMsg);
            prepareMessageService.save(prepareMessage, prepareMessageCollection);

            // 4. 主节点向其他备份节点广播 PrepareMessage
            try {
                netService.broadcastMsg(validatorAddress.getIp(), validatorAddress.getPort(),
                        objectMapper.writeValueAsString(prepareMessage));
            } catch (IOException e) {
                logger.error("广播 PrepareMessage 失败，错误信息为：" + e.getMessage());
            }
        } else if (txMsg != null) {
            logger.error("Tx Message: " + txMsg.getMsgId() + " 未通过校验");
        } else {
            logger.error("Tx Message 为空");
        }
    }

    /**
     * 根据 transaction 生成 message
     *
     * @param txList
     * @return
     */
    public TransactionMessage genInstance(List<Transaction> txList) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(txList, timestamp, pubKey));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new TransactionMessage(msgId, timestamp, pubKey, signature, txList);
    }

    /**
     * 从 tx list 中获取 tx，生成 TransactionMessage
     *
     * @param queueName
     * @param limitTime
     * @param limitSize
     * @return
     */
    public TransactionMessage genInstance(String queueName, double limitTime, double limitSize) {
        RabbitmqUtil rmq = new RabbitmqUtil(queueName);
        List<Transaction> txList = txDao.pull(queueName, limitTime, limitSize);
        if (txList.size() > 0) {
            return this.genInstance(txList);
        } else {
            return null;
        }
    }

    /**
     * 将 TransactionMessage 对象存入集合 collectionName 中。
     *
     * @param txMsg
     * @param collectionName
     * @return
     */
    public boolean save(TransactionMessage txMsg, String collectionName) {
        // 对先读再存方法加锁，防止出现一个线程读的同时另外一个线程改变
        synchronized (TransactionMessageService.class) {
            if (MongoUtil.findByKV("msgId", txMsg.getMsgId(), collectionName)) {
                return false;
            } else {
                MongoUtil.insertJson(txMsg.toString(), collectionName);
                return true;
            }
        }
    }

    /**
     * 将 TransactionMessage json 字符串存入集合 collectionName 中。
     *
     * @param blockMsgStr
     * @param collectionName
     * @return
     */
    public boolean save(String blockMsgStr, String collectionName) {
        TransactionMessage txMsg = null;
        try {
            txMsg = objectMapper.readValue(blockMsgStr, TransactionMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(txMsg, collectionName);
    }

    /**
     * 进行签名和验证的 content
     *
     * @param txList
     * @param timestamp
     * @param pubKey
     * @return
     */
    public String getSignContent(List<Transaction> txList, String timestamp, String pubKey) {
        String msgType = Const.TXM;
        return txList.toString() + msgType + timestamp + pubKey;
    }

    /**
     * 校验数字签名
     *
     * @param txMsg
     * @return
     */
    public boolean verify(TransactionMessage txMsg) {
        return SignatureUtil.verify(txMsg.getPubKey(), getSignContent(txMsg.getTxList(), txMsg.getTimestamp(),
                txMsg.getPubKey()), txMsg.getSignature());
    }
}
