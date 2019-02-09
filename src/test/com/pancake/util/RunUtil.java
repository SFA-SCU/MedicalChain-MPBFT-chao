package com.pancake.util;

import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.TxString;
import com.pancake.entity.enumeration.TxType;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.BlockService;
import com.pancake.service.component.TransactionService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao on 2019/2/3.
 */
public class RunUtil {
    private final static Logger logger = LoggerFactory.getLogger(RunUtil.class);
    private TransactionService txService = TransactionService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    @Test
    /**
     * 向队列中添加 tx
     */
    public void addTxToQueue() {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE);
        List<String> txList = new ArrayList<String>();
        try {
            long startTime = System.currentTimeMillis();
            int textTxCount = 500;
            for (int i = 0; i < textTxCount; i++) {
                Transaction tx = txService.genTx(TxType.INSERT.getName(), new TxString("测试" + i));
                txList.add(tx.toString());
//                rmq.push(tx.toString());
            }
            long endTime = System.currentTimeMillis();
            System.out.println("添加" + textTxCount + "条 tx 的运行时间：" + (endTime - startTime) / 1000.0 + "s");
            rmq.push(txList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 统计各个集合中记录的数量
     */
    @Test
    public void countRecordQuantity() {
        String url;
        String pmCollection;
        String pdmCollection;
        String cmtmCollection;
        String cmtdmCollection;
        String blockChainCollection;
        String txCollection;
        List<String> result = new ArrayList<String>();

        List<NetAddress> netAddresses = JsonUtil.getValidatorAddressList(Const.BlockChainConfigFile);
        MongoDB mongoDB;

        // 1. 检索 Validator 上的所有集合
        for (NetAddress na : netAddresses) {
            url = na.toString();
            mongoDB = new MongoDB(new NetAddress("127.0.0.1", 27017), Const.BLOCK_CHAIN);
            pmCollection = url + "." + Const.PM;
            pdmCollection = url + "." + Const.PDM;
            cmtmCollection = url + "." + Const.CMTM;
            cmtdmCollection = url + "." + Const.CMTDM;
            blockChainCollection = url + "." + Const.BLOCK_CHAIN;
            txCollection = url + "." + Const.TX;
            String lbiCollection = url + "." + Const.LAST_BLOCK_ID;
            String txIdCollectorColl = "TxIdCollector" + na.getIp() + ":" + (na.getPort() + 1000) + ".TxIds";

            long pmCount = mongoDB.countRecords(pmCollection);
            long pdmCount = mongoDB.countRecords(pdmCollection);
            long cmtmCount = mongoDB.countRecords(cmtmCollection);
            long cmtdmCount = mongoDB.countRecords(cmtdmCollection);
            long blockChainCount = mongoDB.countRecords(blockChainCollection);
            long txCount = mongoDB.countRecords(txCollection);
            int blockIdCount = mongoDB.countValuesByKey("blockId", blockChainCollection);
            long txIdsCount = mongoDB.countRecords(txIdCollectorColl);
            String lastBlockId = blockService.getLastBlockId(mongoDB, lbiCollection);

            result.add("主机 [ " + url + " ] < "
                    + ", pmCount: " + pmCount
                    + ", pdmCount: " + pdmCount
                    + ", cmtmCount: " + cmtmCount
                    + ", cmtdmCount: " + cmtdmCount
                    + ", blockChainCount: " + blockChainCount
                    + ", txCount: " + txCount
                    + ", blockIdCount: " + blockIdCount
                    + ", txIdsCount: " + txIdsCount
                    + ", lastBlockId: " + lastBlockId);
        }

        result.add("=================================================================================");

        // 2. 检索 blocker 上的所有集合
        String lbiCollection;
        String txIdCollection;
        String txIdMsgCollection;
        String blockMsgCollection;
        List<NetAddress> blockerList = JsonUtil.getBlockerAddressList(Const.BlockChainConfigFile);
        for(NetAddress blockerAddr : blockerList) {
            mongoDB = new MongoDB(new NetAddress("127.0.0.1", 27017), Const.BLOCK_CHAIN);
            blockChainCollection = blockerAddr + "." + Const.BLOCK_CHAIN;
            lbiCollection = blockerAddr + "." + Const.LAST_BLOCK_ID;
            txIdCollection = blockerAddr + "." + Const.TX_ID;
            txIdMsgCollection = blockerAddr + "." + Const.TIM;
            blockMsgCollection = blockerAddr + "." + Const.BM;

            long blockChainCount = mongoDB.countRecords(blockChainCollection);
            long blockMsgCount = mongoDB.countRecords(blockMsgCollection);
            String lastBlockId = blockService.getLastBlockId(mongoDB, lbiCollection);
            long txIdsCount = mongoDB.countRecords(txIdCollection);
            long txIdMsgCount = mongoDB.countRecords(txIdMsgCollection);
            result.add("主机 [ " + blockerAddr + " ] <  blockChainCount: " + blockChainCount
                    + ", blockMsgCount: " + blockMsgCount
                    + ", txIdsCount: " + txIdsCount
                    + ", txIdMsgCount: " + txIdMsgCount
                    + ", lastBlockId: " + lastBlockId);
        }

        for (String res : result) {
            System.out.println(res);
        }
    }

    @Test
    public void compare() {
        MongoUtil.compare2Collection("127.0.0.1:8001.CommitMsg", "127.0.0.1:8002.CommitMsg");
    }

    @Test
    public void findUnCommitClientMsg() {
        String url;
        String commitMsgCountCollection;
        List<NetAddress> netAddresses = JsonUtil.getValidatorAddressList(Const.BlockChainConfigFile);
        MongoDB mongoDB;

        // 1. 检索 Validator 上的所有集合
        for (NetAddress na : netAddresses) {
            url = na.toString();
            mongoDB = new MongoDB(new NetAddress(na.getIp(), 27017), Const.BLOCK_CHAIN);
            commitMsgCountCollection = url + "." + Const.CMTM_COUNT;
//            System.out.println(commitMsgCountCollection);
            List<String> result = mongoDB.find("committed", false, commitMsgCountCollection);
            for (String str : result) {
                logger.error(commitMsgCountCollection + ": " + str);
            }

        }
    }
}
