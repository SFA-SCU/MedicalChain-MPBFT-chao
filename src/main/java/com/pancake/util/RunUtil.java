package com.pancake.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.Block;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.TxString;
import com.pancake.entity.enumeration.TxType;
import com.pancake.entity.pojo.MongoDBConfig;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.BlockService;
import com.pancake.service.component.TransactionService;
import com.pancake.service.message.impl.BlockMessageService;
import com.pancake.socket.Blocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 junit 以单独运行一个方法
 * Created by chao on 2017/12/11.
 */

public class RunUtil {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(RunUtil.class);
    private BlockService blockService = BlockService.getInstance();
    private BlockMessageService blockMsgServ = BlockMessageService.getInstance();
    private Blocker blocker = new Blocker();
    private TransactionService txService = TransactionService.getInstance();

    public static void main(String[] args) {
        RunUtil runUtil = new RunUtil();
//        runUtil.addTxToQueue();
        runUtil.countRecordQuantity();
//        runUtil.sendGenesisBlock();
    }

    public void sendGenesisBlock() {
        String txId = Const.GENESIS_TX_ID;
        List<String> txIdList = new ArrayList<String>();
        txIdList.add(txId);
        Block block = blockService.genBlock(Const.GENESIS_BLOCK_ID, txIdList);
        System.out.println("block: " + block);
        blocker.sendBlock(block, NetUtil.getPrimaryNode());
    }
    /**
     * 统计各个集合中记录的数量
     */
    public void countRecordQuantity() {
        String url;
        String ppmCollection;
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
            MongoDBConfig mongoDBConfig = JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile);
            mongoDB = new MongoDB(mongoDBConfig);
            ppmCollection = url + "." + Const.PPM;
            pmCollection = url + "." + Const.PM;
            pdmCollection = url + "." + Const.PDM;
            cmtmCollection = url + "." + Const.CMTM;
            cmtdmCollection = url + "." + Const.CMTDM;
            blockChainCollection = url + "." + Const.BLOCK_CHAIN;
            txCollection = url + "." + Const.TX;
            String lbiCollection = url + "." + Const.LAST_BLOCK_ID;
            String txIdCollectorColl = "TxIdCollector" + na.getIp() + ":" + (na.getPort() + 1000) + ".TxIds";

            long ppmCount = mongoDB.countRecords(ppmCollection);
            long pmCount = mongoDB.countRecords(pmCollection);
            long pdmCount = mongoDB.countRecords(pdmCollection);
            long cmtmCount = mongoDB.countRecords(cmtmCollection);
            long cmtdmCount = mongoDB.countRecords(cmtdmCollection);
            long blockChainCount = mongoDB.countRecords(blockChainCollection);
            long txCount = mongoDB.countRecords(txCollection);
            int blockIdCount = mongoDB.countValuesByKey("blockId", blockChainCollection);
            long txIdsCount = mongoDB.countRecords(txIdCollectorColl);
            String lastBlockId = blockService.getLastBlockId(mongoDB, lbiCollection);

            result.add("主机 [ " + url + " ] < ppmCount: " + ppmCount
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

    /**
     * 清空所有集合
     *
     * @throws Exception
     */
    public void dropAllCollections() throws Exception {
        MongoUtil.dropAllCollections();
    }

    /**
     * 向队列中添加 tx
     */
    public void addTxToQueue() {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE);
        List<Transaction> txList = new ArrayList<Transaction>();
        try {
            for (int i = 0; i < 1000; i++) {
                Transaction tx = txService.genTx(TxType.INSERT.getName(), new TxString("测试" + i));
//                if(i % 100 != 0) {
//                    txList.add(tx);
//                    rmq.push(objectMapper.writeValueAsString(txList));
//                }

                rmq.push(tx.toString());
            }
//            rmq.push(objectMapper.writeValueAsString(txList));
//            logger.info(objectMapper.writeValueAsString(txList).substring(0,1));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        List<String> msgList = rmq.pull(100000, 4.0/1024.0);
//        for(String msg : msgList) {
//            System.out.println(msg);
//        }
    }

    public void addVerifiedTxToQueue() {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.VERIFIED_TX_QUEUE);
        try {
            for (int i = 0; i < 50; i++) {
                Transaction tx = txService.genTx(TxType.INSERT.getName(), new TxString("测试" + i));
                rmq.push(tx.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTxIdToQueue() {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_ID_QUEUE);
        try {
            for (int i = 0; i < 1000; i++) {
                Transaction tx = txService.genTx(TxType.INSERT.getName(), new TxString("测试" + i));
                rmq.push(tx.getTxId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void countBlocks() {
        String realIp = NetUtil.getRealIp();
        String url;
        String blockChainCollection;
        for (int port = 8000; port < 8004; port++) {
            url = realIp + ":" + port;
            blockChainCollection = url + "." + Const.BLOCK_CHAIN;
            MongoUtil.findValuesByKey("blockId", blockChainCollection);
        }
    }

    public void showBlockChain() {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + 8000;
        String blockChainCollection = url + "." + Const.BLOCK_CHAIN;
        List<Block> blockList = blockService.getAllBlocks(blockChainCollection);
        int blockNum = 0;
        for (Block block : blockList) {
            System.out.println("Block" + blockNum + ": " + block.toString());
            blockNum++;
        }
    }

}
