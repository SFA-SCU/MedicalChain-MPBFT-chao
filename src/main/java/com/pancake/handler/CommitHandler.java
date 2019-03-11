package com.pancake.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.CommitMessageCount;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.message.ClientMessage;
import com.pancake.entity.message.PrepareMessage;
import com.pancake.entity.message.TransactionMessage;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.TransactionService;
import com.pancake.service.message.impl.MessageService;
import com.pancake.service.message.impl.PrepareMessageService;
import com.pancake.util.MongoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by chao on 2017/12/5.
 */
public class CommitHandler implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(CommitHandler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static PrepareMessageService prepareMessageService = PrepareMessageService.getInstance();
    private final static TransactionService txService = TransactionService.getInstance();
    private NetAddress addr;

    public CommitHandler(NetAddress addr) {
        this.addr = addr;
    }

    public void run() {
        String url = addr.toString();
        String commitMessageCountCollection = url + "." + Const.CMTM_COUNT;
        String txCollection = url + "." + Const.TX;
        String prepareMsgCollection = url + "." + Const.PM;
//        String blockChainCollection = url + "." + Const.BLOCK_CHAIN;
        this.commitTx(commitMessageCountCollection, prepareMsgCollection, txCollection);
    }

    private void commitTx(String commitMessageCountCollection, String prepareMsgCollection, String txCollection) {
        while (true) {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<String> results = MongoUtil.find("committed", false, "clientMsgIdCount",
                    4, commitMessageCountCollection);
            for(String result : results) {
                try {
                    CommitMessageCount commitMessageCount = objectMapper.readValue(result, CommitMessageCount.class);
                    String clientMsgId = commitMessageCount.getClientMsgId();
                    //正式提交来自客户端的提案
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
