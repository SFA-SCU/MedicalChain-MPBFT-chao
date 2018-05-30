package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.message.ClientMessage;
import com.pancake.entity.message.PrePrepareMessage;
import com.pancake.entity.message.PrepareMessage;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.impl.NetService;
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
public class PrePrepareMessageService {
    private final static Logger logger = LoggerFactory.getLogger(PrePrepareMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理接收到的预准备消息
     *
     * @param rcvMsg
     * @param validatorAddr
     * @return
     * @throws IOException
     */
    public static boolean procPPMsg(String rcvMsg, NetAddress validatorAddr) throws IOException {
        String url = validatorAddr.toString();
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 PrePrepareMessage
        PrePrepareMessage ppm = objectMapper.readValue(rcvMsg, PrePrepareMessage.class);
        logger.info("接收到 PrePrepareMsg：" + ppm.getMsgId());
        logger.info("开始校验 PrePrepareMsg ...");
        boolean verifyRes = PrePrepareMessageService.verify(ppm);
        logger.info("校验结束，结果为：" + verifyRes);

        if (verifyRes) {
            // 2. 校验结果为 true ，将 PrePrepareMessage 存入到集合中
            String ppmCollection = url + "." + Const.PPM;
            if (PrePrepareMessageService.save(ppm, ppmCollection)) {
                logger.info("PrePrepareMessage [" + ppm.getMsgId() + "] 已存入数据库");
            } else {
                logger.info("PrePrepareMessage [" + ppm.getMsgId() + "] 已存在");
            }

            // 3. 生成 PrepareMessage，存入集合，并向其他节点进行广播
            PrepareMessage pm = PrepareMessageService.genInstance(ppm.getSignature(), ppm.getViewId(), ppm.getSeqNum(),
                    validatorAddr.getIp(), validatorAddr.getPort());
            String pmCollection = url + "." + Const.PM;
            PrepareMessageService.save(pm, pmCollection);
            logger.info("PrepareMessage [" + pm.getMsgId() + "] 已存入数据库");
            NetService.broadcastMsg(validatorAddr.getIp(), validatorAddr.getPort(), pm.toString());
        }
        return verifyRes;
    }

    @SuppressWarnings("Duplicates")
    public static boolean save(PrePrepareMessage ppMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", ppMsg.getMsgId(), collectionName)) {
            logger.info("ppMsg 消息 [" + ppMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(ppMsg.toString(), collectionName);
            return true;
        }
    }

    public static boolean save(String ppMsgStr, String collectionName) {
        PrePrepareMessage ppMsg = null;
        try {
            ppMsg = objectMapper.readValue(ppMsgStr, PrePrepareMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(ppMsg, collectionName);
    }

    /**
     * 根据 序列号 与 clientMs 对象生成预准备消息对象
     *
     * @param seqNum
     * @param clientMsg
     * @return
     */
    public static PrePrepareMessage genInstance(String seqNum, ClientMessage clientMsg) {
        String timestamp = TimeUtil.getNowTimeStamp();
        // TODO
        String viewId = "1";

        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(clientMsg.getMsgId(), viewId, seqNum, timestamp));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new PrePrepareMessage(msgId, timestamp, pubKey, signature, viewId, seqNum, clientMsg);
    }

    /**
     * 检验 PrePrepareMessage的正确性
     *
     * @param ppm
     * @return
     */
    public static boolean verify(PrePrepareMessage ppm) {
        if (!SignatureUtil.verify(ppm.getPubKey(), getSignContent(ppm.getClientMsg().getMsgId(), ppm.getViewId(),
                ppm.getSeqNum(), ppm.getTimestamp()), ppm.getSignature())) {
            return false;
        }
        return true;
    }

    /**
     * 根据传入的内容生成 ppm 要签名的字符串
     *
     * @param clientMsgId
     * @param viewId
     * @param seqNum
     * @param timestamp
     * @return
     */
    public static String getSignContent(String clientMsgId, String viewId, String seqNum, String timestamp) {
        StringBuilder sb = new StringBuilder();
        sb.append(clientMsgId).append(viewId).append(seqNum).append(timestamp);
        return sb.toString();
    }
}
