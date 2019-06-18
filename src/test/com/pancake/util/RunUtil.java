package com.pancake.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.TxString;
import com.pancake.entity.enumeration.TxType;
import com.pancake.entity.pojo.MongoDBConfig;
import com.pancake.entity.pojo.RabbitmqServer;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.BlockService;
import com.pancake.service.component.TransactionService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chao on 2019/2/3.
 */
public class RunUtil {
    private final static Logger logger = LoggerFactory.getLogger(RunUtil.class);
    private BlockService blockService = BlockService.getInstance();
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static MongoDBConfig mongoDBConfig = JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile);
    private final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:ms");

    static {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").
                setLevel(Level.ERROR);
    }

    @Test
    public void countTx() {

    }

    @Test
    /**
     * 向队列中添加 tx
     */
    public void addTxToQueue() {
//        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE);
        TransactionService txService = TransactionService.getInstance();
        RabbitmqServer rabbitmqServer =
                new RabbitmqServer("admin", "zc-12332145", "127.0.0.1", 5672);
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE, rabbitmqServer);
        List<String> txList = new ArrayList<String>();
        try {
            long startTime = System.currentTimeMillis();
            int textTxCount = 1000;
            for (int i = 0; i < textTxCount; i++) {
                Transaction tx = txService.genTx(TxType.INSERT.getName(), new TxString("测试" + i));
                txList.add(tx.toString());
//                rmq.push(tx.toString());
            }
            long endTime = System.currentTimeMillis();
            System.out.println("添加" + textTxCount + "条 tx 的运行时间：" + (endTime - startTime) / 1000.0 + "s");
            System.out.println("开始向 rabbitmq 发送 tx");
            rmq.push(txList);
            endTime = System.currentTimeMillis();
            System.out.println("发送" + textTxCount + "条 tx 的运行时间：" + (endTime - startTime) / 1000.0 + "s");
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

    /**
     * 统计远程主机上记录的个数
     */
    @Test
    public void countRemoteRecords() throws InterruptedException {
//        NetAddress[] nodes = {new NetAddress("111.230.209.236", 8000),
//                new NetAddress("118.126.89.41", 8000),
//                new NetAddress("193.112.204.149", 8000),
//                new NetAddress("129.204.128.234", 8000)};
        NetAddress[] nodes = getNodes();
//        NetAddress[] prefix = {new NetAddress("172.16.0.6", 8000),
//                new NetAddress("172.16.0.4", 8000),
//                new NetAddress("172.16.0.5", 8000),
//                new NetAddress("172.16.0.10", 8000)};
        NetAddress[] prefix = nodes;
        int nodesCount = nodes.length;
        int mongoPort = 27017;
        String username = "blockchain";
        String password = "zc-12332145";
        String database = "BlockChain";
        String pmCollection;
        String cmtmCollection;
        String txCollection;
        String commitMessageCountCollection;
        MongoDBConfig config;
        MongoDB mongoDB;

        List<String> result = new ArrayList<String>();

        while (true) {
            for (int i = 0; i < nodesCount; i++) {
                config = new MongoDBConfig(nodes[i].getIp(), mongoPort, username, password, database);
                mongoDB = new MongoDB(config);

                pmCollection = prefix[i].toString() + "." + Const.PM;
                cmtmCollection = prefix[i].toString() + "." + Const.CMTM;
                txCollection = prefix[i].toString() + "." + Const.TX;
                commitMessageCountCollection = prefix[i].toString() + "." + Const.CMTM_COUNT;

                long pmCount = mongoDB.countRecords(pmCollection);
                long cmtmCount = mongoDB.countRecords(cmtmCollection);
                long txCount = mongoDB.countRecords(txCollection);
                long commitMessageCount = mongoDB.countRecords(commitMessageCountCollection);

                result.add(df.format(new Date()) + " --> 主机 [ " + nodes[i].toString() + " ] < "
                        + "prepare msg: " + pmCount
                        + ", commit msg: " + cmtmCount
                        + "commitMessageCount: " + commitMessageCount
                        + ", tx count: " + txCount);
            }

            result.add("=================================================================================");

            for (String res : result) {
                System.out.println(res);
            }

            Thread.sleep(1000);
        }

    }

    /**
     * 统计远程主机上交易单与区块的个数
     */
    @Test
    public void countRemoteTxAndBlocks() throws InterruptedException {
//        NetAddress[] nodes = {new NetAddress("111.230.209.236", 8000),
//                new NetAddress("118.126.89.41", 8000),
//                new NetAddress("193.112.204.149", 8000),
//                new NetAddress("129.204.128.234", 8000)};
        NetAddress[] nodes = getNodes();
//        NetAddress[] prefix = {new NetAddress("172.16.0.6", 8000),
//                new NetAddress("172.16.0.4", 8000),
//                new NetAddress("172.16.0.5", 8000),
//                new NetAddress("172.16.0.10", 8000)};
        NetAddress[] prefix = nodes;
        int nodesCount = nodes.length;
        int mongoPort = 27017;
        String username = "blockchain";
        String password = "zc-12332145";
        String database = "BlockChain";
        String txCollection;
        String blockChainCollection;
        MongoDBConfig config;
        MongoDB mongoDB;

        List<String> result = new ArrayList<String>();

        while (true) {
            for (int i = 0; i < nodesCount; i++) {
                config = new MongoDBConfig(nodes[i].getIp(), mongoPort, username, password, database);
                mongoDB = new MongoDB(config);

                txCollection = prefix[i].toString() + "." + Const.TX;
                blockChainCollection = prefix[i].getIp() + ":" + prefix[i].getPort() + "." + Const.BLOCK_CHAIN;

                long txCount = mongoDB.countRecords(txCollection);
                long blocksCount = mongoDB.countRecords(blockChainCollection);

                result.add(df.format(new Date()) + " --> 节点 [ " + prefix[i] + " ] < "
                        + "交易单数量: " + txCount
                        + ", 区块数量: " + blocksCount);
            }

            result.add("=================================================================================");

            for (String res : result) {
                System.out.println(res);
            }

            Thread.sleep(2000);
        }

    }
    @Test
    public void compare() {
        MongoUtil.compare2Collection("127.0.0.1:8001.CommitMsg", "127.0.0.1:8002.CommitMsg");
    }

    @Test
    public void findUnCommitClientMsg() {
        String url;
        int mongoPort = 27017;
        String username = "blockchain";
        String password = "zc-12332145";
        String database = "BlockChain";

        String commitMsgCountCollection;
        NetAddress[] nodes = getNodes();
        MongoDBConfig config;
        MongoDB mongoDB;

        // 1. 检索 Validator 上的所有集合
        for (NetAddress na : nodes) {
            url = na.toString();
            config = new MongoDBConfig(na.getIp(), mongoPort, username, password, database);
            mongoDB = new MongoDB(config);
//            mongoDB = new MongoDB(new NetAddress(na.getIp(), 27017), Const.BLOCK_CHAIN);
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

        NetAddress[] nodes = getNodes();
        int nodesCount = nodes.length;

        Map<String, Object> mongoMap = new LinkedHashMap<String, Object>();
        mongoMap.put("ip", "127.0.0.1");
        mongoMap.put("port", 27017);
        mongoMap.put("username", "blockchain");
        mongoMap.put("password", "zc-12332145");
        mongoMap.put("database", "BlockChain");

        Map<String, Object> rabbitMap = new LinkedHashMap<String, Object>();
        rabbitMap.put("ip", "127.0.0.1");
        rabbitMap.put("port", 5672);
        rabbitMap.put("userName", "admin");
        rabbitMap.put("password", "zc-12332145");

        Map<String, Object> keyPairMap = new LinkedHashMap<String, Object>();
        keyPairMap.put("pvt_key_file", "./privateKey.txt");
        keyPairMap.put("pub_key_file", "./publicKey.txt");

        Map<String, Object> txTransmitterConfigMap = new LinkedHashMap<String, Object>();
        txTransmitterConfigMap.put("limitTime", 200); //ms
        txTransmitterConfigMap.put("limitSize", 0.08); //MB

        List<NetAddress> validatorList = new ArrayList<NetAddress>();
        for (int i = 0; i < nodesCount;i++) {
            validatorList.add(nodes[i]);
        }

        Map<String, Object> configMap;
        String filePath;
        String configData;
        for (int i = 0; i < nodesCount;i++) {
            configMap = new LinkedHashMap<String, Object>();
            configData = "";

            configMap.put("current_validator", nodes[i]);
            configMap.put("validators", validatorList);
            configMap.put("mongodb", mongoMap);
            configMap.put("rabbitmq", rabbitMap);
            configMap.put("key_pair", keyPairMap);
            configMap.put("tx_transmitter", txTransmitterConfigMap);

            try {
                configData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configMap);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            filePath = "node" + (i + 1) + ".json";

            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(new File(filePath));
                outputStream.write(configData.getBytes(), 0, configData.length());
                outputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 删除数据库中所有集合
     */
    @Test
    public void clearDB() {
//        NetAddress[] nodes = {new NetAddress("111.230.209.236", 8000),
//                new NetAddress("118.126.89.41", 8000),
//                new NetAddress("193.112.204.149", 8000),
//                new NetAddress("129.204.128.234", 8000)};
        NetAddress[] nodes = getNodes();
        int nodesCount = nodes.length;
        int mongoPort = 27017;
        String username = "blockchain";
        String password = "zc-12332145";
        String database = "BlockChain";
        MongoDBConfig mongoDBConfig = null;
        MongoDB mongoDB = null;
        for (int i = 0; i < nodesCount; i++) {
            mongoDBConfig = new MongoDBConfig(nodes[i].getIp(), mongoPort, username, password, database);
            mongoDB = new MongoDB(mongoDBConfig);
            mongoDB.deleteAllCollections();
        }
    }

    @Test
    public void split() {
        String str = "{\"msgId\":\"KQDaXx4c9CEJ4UPuyfvu589lTQwPtzz0/wzmdURHPKw=\",\"msgType\":\"CommitMsg\",\"timestamp\":\"1549962482980\",\"pubKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE5w13lRiN+JqTnDbbZR76bs/p/JoMyS1puMRiLzQGCokR9B9OBh/7+1mtSoQ1d1wMAYDfLotETYR4npFzovUFYQ==\",\"signature\":\"MEUCIQDxbXCBNkqlHiXUpJ5Zuv/x0BoNeGfCwvSpgiqtqVBekQIgQ2VnNNEFCeLsg/XpvpssP7BiPYbfPc663buTf+pBxac=\",\"prepareMessageId\":\"iPul8GyBpRSaLHPtI7pBprIHSBXNvFyc2C9J7jkkkeE=\",\"clientMsgId\":\"yGZnXsWQfgprPxUTN6K75AbgjwLZ2T3tSXAPc/vB8cc=\",\"ip\":\"111.230.197.199\",\"port\":8000}";
        System.out.println(str.split("\"")[3]);
    }

    @Test
    public void time() {
            String[] time1 = "16:12:46:1246".split(":");
        String[] time2  = "16:12:55:1255".split(":");
        int minute;
        int sec;
        int millsec;
        double total;
        minute = Integer.parseInt(time2[1]) - Integer.parseInt(time1[1]);
        sec = Integer.parseInt(time2[2]) - Integer.parseInt(time1[2]);
        millsec = Integer.parseInt(time2[3]) - Integer.parseInt(time1[3]);
        double tmp;
        if (millsec < 0) {
            tmp = Double.parseDouble("-0." + Math.abs(millsec));
        } else {
            tmp = Double.parseDouble("0." + millsec);
        }
        total = minute * 60 + sec + tmp;
        logger.info(String.valueOf(total) + "s");
    }

    public NetAddress[] getNodes() {
//        NetAddress[] nodes = {new NetAddress("111.230.200.49", 8000),
//                new NetAddress("193.112.250.122", 8000),
//                new NetAddress("193.112.201.81", 8000),
//                new NetAddress("134.175.208.12", 8000)};
        NetAddress[] nodes = {new NetAddress("127.0.0.1", 8000),
                new NetAddress("127.0.0.1", 8001),
                new NetAddress("127.0.0.1", 8002),
                new NetAddress("127.0.0.1", 8003)};
//        NetAddress[] nodes = {new NetAddress("192.168.12.128", 8000),
//                new NetAddress("192.168.12.129", 8000),
//                new NetAddress("192.168.12.130", 8000),
//                new NetAddress("192.168.12.131", 8000)};
        return nodes;
    }
}
