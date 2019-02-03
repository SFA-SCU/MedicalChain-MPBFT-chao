package com.pancake.service.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.Block;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.message.*;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.BlockService;
import com.pancake.service.component.NetService;
import com.pancake.service.component.TransactionService;
import com.pancake.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chao on 2017/11/21.
 */
public class CommittedMessageService {
    private final static Logger logger = LoggerFactory.getLogger(CommittedMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private TransactionService txService = TransactionService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    private BlockMessageService blockMsgServ = BlockMessageService.getInstance();
    private LastBlockIdMessageService lbmService = LastBlockIdMessageService.getInstance();
    private TxIdMessageService timSrv = TxIdMessageService.getInstance();
    private NetService netService = NetService.getInstance();
    private BlockMessageService blockMessageService = BlockMessageService.getInstance();

    private static class LazyHolder {
        private static final CommittedMessageService INSTANCE = new CommittedMessageService();
    }

    private CommittedMessageService() {
    }

    public static CommittedMessageService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 承认 CommittedMessage 后，将 CommittedMessage 以及其认可的 clientMessage 中的内容（Transaction，Block）
     * 存储到数据库中
     *
     * @param cmtdMsg
     * @param clientMessage
     * @param netAddress
     */
    @SuppressWarnings("Duplicates")
    public void procCMTDM(CommittedMessage cmtdMsg, ClientMessage clientMessage, NetAddress netAddress) {
        String url = netAddress.toString();
        String blockChainCollection = url + "." + Const.BLOCK_CHAIN;
        String txCollection = url + "." + Const.TX;
        String cmtdMsgCollection = url + "." + Const.CMTDM;
        String lbiCollection = url + "." + Const.LAST_BLOCK_ID;
        String cliMsgType = clientMessage.getClass().getSimpleName();
//        NetAddress publisherAddr = JsonUtil.getPublisherAddress(Const.BlockChainConfigFile);
        // TODO
        NetAddress blockerAddr = new NetAddress(netAddress.getIp(), netAddress.getPort() + 1000);

        if (this.save(cmtdMsg, cmtdMsgCollection)) {
            logger.info("将 CommittedMessage [" + cmtdMsg.toString() + "] 存入数据库");

            if (cliMsgType.equals(BlockMessage.class.getSimpleName())) {
                // 如果 clientMessage 引用的对象为 BlockMessage 类型
                BlockMessage blockMessage = (BlockMessage) clientMessage;
                Block block = blockMessage.getBlock();
                String blockId = block.getBlockId();
                if (blockService.save(block, blockChainCollection)) {
                    logger.info("区块 " + blockId + " 存入成功");
                    if (blockService.updateLastBlockId(blockId, lbiCollection)) {
                        logger.info("Last block Id: " + blockId + " 更新成功");

                        // 验证成功的 block 发送到 Blocker 服务器上
                        netService.sendMsg(blockMessageService.genInstance(block).toString(), blockerAddr.getIp(),
                                blockerAddr.getPort());
//                            new NettyClient(publisherAddr.getIp(), publisherAddr.getPort()).start(lbMsg.toString());
                    } else {
                        logger.error("Last block Id: " + blockId + " 更新失败");
                    }

                } else {
                    logger.warn("区块 " + block.getBlockId() + " 存入失败");
                }
            } else if (cliMsgType.equals(TransactionMessage.class.getSimpleName())) {
                // 如果 clientMessage 引用的对象为 TransactionMessage 类型
                TransactionMessage txMessage = (TransactionMessage) clientMessage;
                List<Transaction> txList = txMessage.getTxList();
                if (txService.saveBatch(txList, txCollection)) {
                    List<String> txIdList = txService.getTxIdList(txList);
                    logger.info("交易 :" + txIdList + " 存入成功");

                    // 验证成功的 tx 发送到 blocker 服务器上
                    TxIdMessage txIdMsg = timSrv.genInstance(txIdList, netAddress.getIp(), netAddress.getPort());
                    netService.sendMsg(txIdMsg.toString(), blockerAddr.getIp(), blockerAddr.getPort());
                }
            } else {
                logger.error("clientMessage的类型为：" + clientMessage.getClass().getSimpleName());
            }

        } else {
            logger.info("CommittedMessage [" + cmtdMsg.getMsgId() + "] 已存在");
        }
    }

    public CommittedMessage genInstance(String cliMsgId, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(cliMsgId, viewId, seqNum, timestamp, ip, port));
        String msgId = SignatureUtil.getSha256Base64(signature);
        return new CommittedMessage(msgId, timestamp, pubKey, signature, cliMsgId, viewId, seqNum, ip, port);
    }

    private String getSignContent(String cliMsgId, String viewId, String seqNum, String timestamp, String ip, int port) {
        return cliMsgId + viewId + seqNum + timestamp + ip + port;
    }

    /**
     * 保存 CommittedMessage
     * @param cmtdm
     * @param collectionName
     * @return
     */
    public boolean save(CommittedMessage cmtdm, String collectionName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("viewId", cmtdm.getViewId());
        map.put("seqNum", cmtdm.getSeqNum());
        return MongoUtil.upSertJson(map, cmtdm.toString(), collectionName);
    }

    public boolean verify(CommittedMessage cm) {
        return SignatureUtil.verify(cm.getPubKey(), getSignContent(cm.getCliMsgId(), cm.getViewId(),
                cm.getSeqNum(), cm.getTimestamp(), cm.getIp(), cm.getPort()), cm.getSignature());
    }

    public static void main(String[] args) {
        CommittedMessageService cmtdmService = CommittedMessageService.getInstance();
        String cm1 = "{\"msgId\":\"BGT/af6pamjrgkbBdrq/e4oiUdkqUpMUi4Eanb69AyI=\",\"msgType\":\"CommittedMsg\",\"timestamp\":\"1512975185325\",\"pubKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKp7wRlLhTDte3wC4Sd7pj42fBCMtNmUQ8cRsSTkY6Pw+qPuytjn4455A6/p7xOHJO8RIO9TRrGLLAnFJGHoZqg==\",\"signature\":\"MEYCIQD3APVH3QK6uOLfj68g3cocZF4GQ4ETYGf/4e0sfrAn/QIhAPSVimBJB9NOtkLfEfMaSGjXZWO5x78AEr4I+hh6bDLa\",\"cliMsgId\":\"VhK1/i9rMcERHy1Ajk8QIYBvTTwGYeRn9A5zdsjb+bA=\",\"viewId\":\"1\",\"seqNum\":\"7\",\"ip\":\"202.115.53.57\",\"port\":8003}";
        String cm2 = "{\"msgId\":\"qKjlWKs3tvAbv4WJqcqmSr5FOgyMmpMvlg9nhc5QT/E=\",\"msgType\":\"CommittedMsg\",\"timestamp\":\"1512975185325\",\"pubKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKp7wRlLhTDte3wC4Sd7pj42fBCMtNmUQ8cRsSTkY6Pw+qPuytjn4455A6/p7xOHJO8RIO9TRrGLLAnFJGHoZqg==\",\"signature\":\"MEUCIQDe/ZguVvVtj0d+7xYBz0B8vENNDPI9aUXrqNfbzxOQ5QIgPj9JiHNgNLL2No36NGG7zM+LDbKGBotFwm5Dlv8mH+4=\",\"cliMsgId\":\"VhK1/i9rMcERHy1Ajk8QIYBvTTwGYeRn9A5zdsjb+bA=\",\"viewId\":\"1\",\"seqNum\":\"7\",\"ip\":\"202.115.53.57\",\"port\":8003}";
        try {
            CommittedMessage committedMessage1 = objectMapper.readValue(cm1, CommittedMessage.class);
            CommittedMessage committedMessage2 = objectMapper.readValue(cm2, CommittedMessage.class);
            System.out.println(cmtdmService.verify(committedMessage1));
            System.out.println(cmtdmService.verify(committedMessage2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
