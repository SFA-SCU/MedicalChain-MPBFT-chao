package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.message.CommitMessage;
import com.pancake.entity.message.PrePrepareMessage;
import com.pancake.entity.message.PrepareMessage;
import com.pancake.entity.message.PreparedMessage;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.impl.NetService;
import com.pancake.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;


/**
 * Created by chao on 2017/12/18.
 */
public class PrepareMessageService {
    private final static Logger logger = LoggerFactory.getLogger(PrepareMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理准备消息
     * 只要准备消息的签名是正确的，它们的视图编号等于副本的当前视图，并且它们的序列号介于 h 和 H，
     * 副本节点（包括主节点）便接受准备消息，并将它们添加到日志中。
     *
     * @param rcvMsg
     * @param validatorAddr
     * @return
     */
    public static boolean procPMsg(String rcvMsg, NetAddress validatorAddr) throws IOException {
        String url = validatorAddr.toString();
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 PrepareMessage
        PrepareMessage pm = objectMapper.readValue(rcvMsg, PrepareMessage.class);
        logger.info("接收到 PrepareMsg：" + rcvMsg);
        logger.info("开始校验 PrepareMsg ...");
        boolean verifyRes = PrepareMessageService.verify(pm);
        logger.info("校验 PrepareMsg 结果为：" + verifyRes);

        if (verifyRes) {
            String pmCollection = url + "." + Const.PM;
            String ppmCollection = url + "." + Const.PPM;
            String cmtmCollection = url + "." + Const.CMTM;
            // 2.  PrepareMessage 存入前检验
            PrePrepareMessage ppm = MongoUtil.findPPMById(SignatureUtil.getSha256Base64(pm.getPpmSign()), ppmCollection);

            // 3. 将 PrePrepareMessage 存入到集合中
            if (PrepareMessageService.save(pm, pmCollection)) {
                logger.info("PrepareMessage [" + pm.getMsgId() + "] 存入数据库");
            } else {
                logger.info("PrepareMessage [" + pm.getMsgId() + "] 已存在");
            }

            //  统计 ppmSign 出现的次数
            int count = MongoUtil.countPPMSign(pm.getPpmSign(), pm.getViewId(), pm.getSeqNum(), pmCollection);
            logger.debug("count = " + count);
            // 4. 达成 count >= 2 * f 后存入到集合中
            if (2 * PeerUtil.getFaultCount() + 1 <= count) {
                logger.info("开始生成 PreparedMessage 并存入数据库");
                String pdmCollection = url + "." + Const.PDM;
                PreparedMessage pdm = PreparedMessageService.genInstance(ppm.getClientMsg().getMsgId(), ppm.getViewId(),
                        ppm.getSeqNum(), validatorAddr.getIp(), validatorAddr.getPort());
                if (PreparedMessageService.save(pdm, pdmCollection)) {
                    logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存入数据库");
                    CommitMessage cmtm = CommitMessageService.genCommitMsg(ppm.getSignature(), ppm.getViewId(),
                            ppm.getSeqNum(), validatorAddr.getIp(), validatorAddr.getPort());
                    logger.info("commit message: " + cmtm.toString());
                    if (CommitMessageService.save(cmtm, cmtmCollection)) {
                        logger.info("CommitMessage [" + cmtm.getMsgId() + "] 已存入数据库");
                        NetService.broadcastMsg(validatorAddr.getIp(), validatorAddr.getPort(), cmtm.toString());
                    } else {
                        logger.info("CommitMessage [" + pdm.getMsgId() + "] 已存在");
                    }
                } else {
                    logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存在");
                }
            } else {
                logger.info("Prepare Message 数量不够");
            }

            // 5. 生成 commit message 存入集合中，并广播给其他节点

        }
        return true;
    }

    /**
     * 根据相关字段生成 PrepareMessage
     *
     * @param ppmSign ppm消息中的数字签名，相当于消息 m 的 digest d.
     * @param viewId
     * @param seqNum
     * @param ip
     * @param port
     * @return
     */
    public static PrepareMessage genInstance(String ppmSign, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(timestamp, pubKey, viewId, seqNum, ppmSign, ip, port));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new PrepareMessage(msgId, timestamp, pubKey, signature, viewId, seqNum, ppmSign, ip, port);

    }

    public static boolean verify(PrepareMessage pm) {
        if (!SignatureUtil.verify(pm.getPubKey(), getSignContent(pm.getTimestamp(), pm.getPubKey(), pm.getViewId(),
                pm.getSeqNum(), pm.getPpmSign(), pm.getIp(), pm.getPort()), pm.getSignature())) {
            return false;
        }
        return true;
    }

    /**
     * 根据传入的内容生成 pm 要签名的字符串
     *
     * @param ppmSign   ppm消息中的数字签名，相当于消息 m 的 digest d.
     * @param viewId
     * @param seqNum
     * @param timestamp
     * @return
     */
    public static String getSignContent(String timestamp, String pubKey, String viewId, String seqNum, String ppmSign, String ip, int port) {
        return timestamp + pubKey + viewId + seqNum + ppmSign + ip + port;
    }

    public static boolean save(PrepareMessage pMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", pMsg.getMsgId(), collectionName)) {
            logger.info("pMsg 消息 [" + pMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(pMsg.toString(), collectionName);
            return true;
        }
    }

    public static boolean save(String pMsgStr, String collectionName) {
        PrepareMessage pMsg = null;
        try {
            pMsg = objectMapper.readValue(pMsgStr, PrepareMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(pMsg, collectionName);
    }
}
