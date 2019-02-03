package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.Block;
import com.pancake.entity.message.BlockMessage;
import com.pancake.entity.message.PrePrepareMessage;
import com.pancake.entity.message.PrepareMessage;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.BlockService;
import com.pancake.service.component.BlockerService;
import com.pancake.service.component.NetService;
import com.pancake.service.component.TxIdService;
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
public class BlockMessageService {
    private final static Logger logger = LoggerFactory.getLogger(BlockMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private TxIdService tis = TxIdService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    private BlockerService blockerService = BlockerService.getInstance();
    private NetService netService = NetService.getInstance();
    private PrepareMessageService prepareMessageService = PrepareMessageService.getInstance();

    private static class LazyHolder {
        private static final BlockMessageService INSTANCE = new BlockMessageService();
    }
    private BlockMessageService (){}
    public static BlockMessageService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 处理客户端发送的消息
     * @param rcvMsg 接收到的消息
     * @param validatorAddr 本机的url
     * @throws IOException
     */
    public void procBlockMsg(String rcvMsg, NetAddress validatorAddr) throws Exception {
        String url = validatorAddr.toString();
        logger.info("本机地址为：" + url);
        // 1. 将从客户端收到的 Block Message 存入到集合中
        String blockMsgCollection = url + "." + Const.BM;
        if(this.save(rcvMsg, blockMsgCollection)) {
            logger.info("Block Message 存入成功");
        } else {
            logger.info("Block Message 已存在");
        }

        // 2. 从集合中取出给当前 PrePrepareMessage 分配的序列号
        long seqNum = MessageService.updateSeqNum(url + ".seqNum");

        // 3. 根据 Block Message 生成 PrePrepareMessage，存入到集合中
        String ppmCollection = url + "." + Const.PPM;
        BlockMessage blockMsg = objectMapper.readValue(rcvMsg, BlockMessage.class);
        PrepareMessage ppm = prepareMessageService.genInstance( blockMsg);
        prepareMessageService.save(ppm, ppmCollection);

        // 4. 主节点向其他备份节点广播 PrePrepareMessage
        NetService.broadcastMsg(validatorAddr.getIp(), validatorAddr.getPort(), objectMapper.writeValueAsString(ppm));
    }

    /**
     * 处理从 Validator 发往 Blocker 的 BlockMessage
     * @param blockMsg
     * @param blockMsgCollection
     * @param txIdCollection
     * @param blockChainCollection
     * @param lbiCollection
     */
    public void procBlockerBlockMsg(BlockMessage blockMsg, NetAddress netAddr, String blockMsgCollection,
                                    String txIdCollection, String blockChainCollection, String lbiCollection) {

        // 1. 将从客户端收到的 Block Message 存入到集合中
        if(this.save(blockMsg, blockMsgCollection)) {
            logger.info("Block Message 存入成功");
            Block block = blockMsg.getBlock();
            String blockId = block.getBlockId();
            if (blockService.save(block, blockChainCollection)) {
                logger.info("区块 " + blockId + " 存入成功");
                if (blockService.updateLastBlockId(blockId, lbiCollection)) {
                    logger.info("Last block Id: " + blockId + " 更新成功");

                } else {
                    logger.error("Last block Id: " + blockId + " 更新失败");
                }
            }
        } else {
            logger.info("Block Message 已存在");
        }

        // 2. 根据 block 中的 TxId，将 txIdCollection 中的相应 TxId 的 InBLock 为设为 true
        for (String txId : blockMsg.getBlock().getTxIdList()) {
            tis.setTrue(txId, txIdCollection);
        }

    }

    /**
     * 根据 block 对象，生成 BlockMessage 对象
     *
     * @param block
     * @return
     */
    public BlockMessage genInstance(Block block) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(block, timestamp, pubKey));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new BlockMessage(msgId, timestamp, pubKey, signature, block);
    }

    /**
     * 将 BlockMessage json 字符串存入集合 collectionName 中。
     *
     * @param blockMsgStr
     * @param collectionName
     * @return
     */
    public boolean save(String blockMsgStr, String collectionName) {
        BlockMessage blockMsg = null;
        try {
            blockMsg = objectMapper.readValue(blockMsgStr, BlockMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(blockMsg, collectionName);
    }

    /**
     * 将 BlockMessage 对象存入集合 collectionName 中。
     *
     * @param blockMsg
     * @param collectionName
     * @return
     */
    public boolean save(BlockMessage blockMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", blockMsg.getMsgId(), collectionName)) {
            logger.info("blockMsg 消息 [" + blockMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(blockMsg.toString(), collectionName);
            return true;
        }
    }

    /**
     * 进行签名和验证的 content
     *
     * @param block
     * @param timestamp
     * @param pubKey
     * @return
     */
    public String getSignContent(Block block, String timestamp, String pubKey) {
        String msgType = Const.BM;
        return block.toString() + msgType + timestamp + pubKey;
    }

    /**
     * 校验数字签名
     *
     * @param blockMsg
     * @return
     */
    public boolean verify(BlockMessage blockMsg) {
        return SignatureUtil.verify(blockMsg.getPubKey(), getSignContent(blockMsg.getBlock(), blockMsg.getTimestamp(),
                blockMsg.getPubKey()), blockMsg.getSignature());
    }
}
