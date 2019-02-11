package com.pancake.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.TxString;
import com.pancake.entity.enumeration.TxType;
import com.pancake.entity.pojo.MongoDBConfig;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.BlockService;
import com.pancake.service.component.TransactionService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chao on 2019/2/3.
 */
public class RunUtil {
    private final static Logger logger = LoggerFactory.getLogger(RunUtil.class);
    private TransactionService txService = TransactionService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static MongoDBConfig mongoDBConfig = JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile);

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
        String cmtmCollection;
        String blockChainCollection;
        String txCollection;
        List<String> result = new ArrayList<String>();

        List<NetAddress> netAddresses = JsonUtil.getValidatorAddressList(Const.BlockChainConfigFile);
        MongoDB mongoDB;


        // 1. 检索 Validator 上的所有集合
        for (NetAddress na : netAddresses) {
            url = na.toString();

            mongoDB = new MongoDB(mongoDBConfig);
            pmCollection = url + "." + Const.PM;
            cmtmCollection = url + "." + Const.CMTM;
            blockChainCollection = url + "." + Const.BLOCK_CHAIN;
            txCollection = url + "." + Const.TX;
            String lbiCollection = url + "." + Const.LAST_BLOCK_ID;
            String txIdCollectorColl = "TxIdCollector" + na.getIp() + ":" + (na.getPort() + 1000) + ".TxIds";

            long pmCount = mongoDB.countRecords(pmCollection);
            long cmtmCount = mongoDB.countRecords(cmtmCollection);
            long blockChainCount = mongoDB.countRecords(blockChainCollection);
            long txCount = mongoDB.countRecords(txCollection);
            int blockIdCount = mongoDB.countValuesByKey("blockId", blockChainCollection);
            long txIdsCount = mongoDB.countRecords(txIdCollectorColl);
            String lastBlockId = blockService.getLastBlockId(mongoDB, lbiCollection);

            result.add("主机 [ " + url + " ] < "
                    + "prepare msg: " + pmCount
                    + ", commit msg: " + cmtmCount
                    + ", tx count: " + txCount
                    + ", blockchain count: " + blockChainCount
                    + ", block id count: " + blockIdCount
                    + ", tx ids count: " + txIdsCount
                    + ", last block id: " + lastBlockId);
        }

        result.add("=================================================================================");

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

    /**
     * 批量生成多个节点的配置文件
     */
    @Test
    public void generateConfigFile() {

        NetAddress validator1 = new NetAddress("127.0.0.1", 8000);
        NetAddress validator2 = new NetAddress("127.0.0.1", 8001);
        NetAddress validator3 = new NetAddress("127.0.0.1", 8002);
        NetAddress validator4 = new NetAddress("127.0.0.1", 8003);

        int validatorCount = 4;

        List<NetAddress> validatorList = new ArrayList<NetAddress>();
        validatorList.add(validator1);
        validatorList.add(validator2);
        validatorList.add(validator3);
        validatorList.add(validator4);


        Map<String, Object> configMap1 = new LinkedHashMap<String, Object>();
        Map<String, Object> configMap2 = new LinkedHashMap<String, Object>();
        Map<String, Object> configMap3 = new LinkedHashMap<String, Object>();
        Map<String, Object> configMap4 = new LinkedHashMap<String, Object>();
        // 1
        configMap1.put("current_validator", validator1);
        configMap2.put("current_validator", validator2);
        configMap3.put("current_validator", validator3);
        configMap4.put("current_validator", validator4);
        // 2
        configMap1.put("validators", validatorList);
        configMap2.put("validators", validatorList);
        configMap3.put("validators", validatorList);
        configMap4.put("validators", validatorList);

        Map<String, Object> mongoMap = new LinkedHashMap<String, Object>();
        mongoMap.put("ip", "127.0.0.1");
        mongoMap.put("port", 27017);
        mongoMap.put("username", "blockchain");
        mongoMap.put("password", "zc-12332145");
        mongoMap.put("database", "BlockChain");
        // 3
        configMap1.put("mongodb", mongoMap);
        configMap2.put("mongodb", mongoMap);
        configMap3.put("mongodb", mongoMap);
        configMap4.put("mongodb", mongoMap);

        Map<String, Object> rabbitMap = new LinkedHashMap<String, Object>();
        rabbitMap.put("ip", "127.0.0.1");
        rabbitMap.put("port", 5672);
        rabbitMap.put("userName", "admin");
        rabbitMap.put("password", "admin");
        // 3
        configMap1.put("rabbitmq", rabbitMap);
        configMap2.put("rabbitmq", rabbitMap);
        configMap3.put("rabbitmq", rabbitMap);
        configMap4.put("rabbitmq", rabbitMap);

        Map<String, Object> keyPairMap = new LinkedHashMap<String, Object>();
        keyPairMap.put("pvt_key_file", "./privateKey.txt");
        keyPairMap.put("pub_key_file", "./publicKey.txt");
        // 4
        configMap1.put("key_pair", keyPairMap);
        configMap2.put("key_pair", keyPairMap);
        configMap3.put("key_pair", keyPairMap);
        configMap4.put("key_pair", keyPairMap);

        Map<String, Object> txTransmitterConfigMap = new LinkedHashMap<String, Object>();
        txTransmitterConfigMap.put("limitTime", 500); //ms
        txTransmitterConfigMap.put("limitSize", 4); //MB
        // 5
        configMap1.put("tx_transmitter", txTransmitterConfigMap);
        configMap2.put("tx_transmitter", txTransmitterConfigMap);
        configMap3.put("tx_transmitter", txTransmitterConfigMap);
        configMap4.put("tx_transmitter", txTransmitterConfigMap);

        String filePath1 = "node1.json";
        String filePath2 = "node2.json";
        String filePath3 = "node3.json";
        String filePath4 = "node4.json";

        String configData1 = "";
        String configData2 = "";
        String configData3 = "";
        String configData4 = "";
        try {
            configData1 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configMap1);
            configData2 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configMap2);
            configData3 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configMap3);
            configData4 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configMap4);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(filePath1));
            outputStream.write(configData1.getBytes(), 0, configData1.length());

            outputStream = new FileOutputStream(new File(filePath2));
            outputStream.write(configData2.getBytes(), 0, configData2.length());

            outputStream = new FileOutputStream(new File(filePath3));
            outputStream.write(configData3.getBytes(), 0, configData3.length());

            outputStream = new FileOutputStream(new File(filePath4));
            outputStream.write(configData4.getBytes(), 0, configData4.length());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 删除数据库中所有集合
     */
    @Test
    public void clearDB() {
        MongoDB mongoDB = new MongoDB(mongoDBConfig);
        mongoDB.deleteAllCollections();
    }
}
