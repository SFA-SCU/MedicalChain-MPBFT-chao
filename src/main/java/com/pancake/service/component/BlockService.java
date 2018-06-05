package com.pancake.service.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.dao.BlockDao;
import com.pancake.entity.component.Block;
import com.pancake.entity.component.MerkleTree;
import com.pancake.entity.content.TxString;
import com.pancake.entity.enumeration.TxType;
import com.pancake.entity.pojo.MongoDBConfig;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pancake.util.SignatureUtil.getSha256Base64;
import static com.pancake.util.SignatureUtil.loadPubKeyStr;
import static com.pancake.util.SignatureUtil.loadPvtKey;


/**
 * Created by chao on 2017/11/17.
 */
public class BlockService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(BlockService.class);
    private BlockDao blockDao = BlockDao.getInstance();
    private TransactionService txService = TransactionService.getInstance();

    private static class LazyHolder {
        private static final BlockService INSTANCE = new BlockService();
    }
    private BlockService (){}
    public static BlockService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 到配置文件中的mongodb查询主节点中的 block
     * @param blockId
     * @return
     */
    public Block findById(String blockId) {
        return this.findById(blockId, JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile), NetUtil.getPrimaryNode());
    }

    /**
     * 根据 TxId 到指定 mongodb 获取 block
     * @param blockId
     * @param mongoDBConfig
     * @return
     */
    public Block findById(String blockId, MongoDBConfig mongoDBConfig, NetAddress netAddress) {
        MongoDB mongoDB = new MongoDB(mongoDBConfig);
        String blockCollection = netAddress + "." + Const.BLOCK_CHAIN;
        List<String> list = mongoDB.find("blockId", blockId, blockCollection);
        //noinspection Duplicates
        if (list.size() == 0)
            return null;
        else if (list.size() == 1) {
            Block block = null;
            try {
                block = objectMapper.readValue(list.get(0), Block.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return block;
        } else {
            logger.error("id 为： " + blockId + " 的block记录存在多条");
            return null;
        }
    }

    /**
     * 到配置文件中的mongodb查询主节点中的 block
     * @return
     */
    public List<Block> findAll() {
        return this.findAll(JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile), NetUtil.getPrimaryNode());
    }

    /**
     * 查询区块链中所有区块
     * @param validatorMongodb
     * @param validatorAddr
     * @return
     */
    public List<Block> findAll(MongoDBConfig validatorMongodb, NetAddress validatorAddr) {
        MongoDB mongoDB = new MongoDB(validatorMongodb);
        String blockCollection = validatorAddr + "." + Const.BLOCK_CHAIN;
        List<Block> blocks = new ArrayList<Block>();
        List<String> list = null;
        list = mongoDB.findAllSort(blockCollection, "timestamp", Const.DESC);

        if (list != null) {
            for (String str : list) {
                try {
                    blocks.add(objectMapper.readValue(str, Block.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return blocks;
        }

        return null;
    }

    /**
     * 根据如下参数算出当前区块的id，并构造Block对象
     *
     * @param preBlockId
     * @param treeHash
     * @param timestamp
     * @param txCount
     * @param txIdList
     * @return
     */
    public Block genBlock(String preBlockId, String treeHash, String timestamp, int txCount, List<String> txIdList) {
        String sigContent = preBlockId + treeHash + timestamp;
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, sigContent);
        String blockId = getSha256Base64(signature);
        return new Block(blockId, preBlockId, treeHash, timestamp, txCount, txIdList, pubKey, signature);
    }

    /**
     * 根据 preBlockId, tx list 生成一个区块
     *
     * @param preBlockId
     * @param txIdList
     * @return
     */
    public Block genBlock(String preBlockId, List<String> txIdList) {
        String treeHash = new MerkleTree(txIdList).getRoot();
        String timestamp = TimeUtil.getNowTimeStamp();
        int txCount = txIdList.size();
        return genBlock(preBlockId, treeHash, timestamp, txCount, txIdList);
    }

    /**
     * 从 RabbitMQ 队列中获取 tx list，生成区块
     * @param preBlockId
     * @param queueName tx list 所在队列的名字
     * @param limitTime 接收一个区块里 tx list 所等待的最长时间
     * @param limitSize 区块大小限制
     * @return
     */
    public Block genBlock(String preBlockId, String queueName, double limitTime, double limitSize){
        RabbitmqUtil rmq = new RabbitmqUtil(queueName);
        List<String> pullContent = rmq.pull(limitTime, limitSize);
        List<String> txIdList = new ArrayList<String>();
        for (String content : pullContent) {
            // 判断 json 是 tx id 对象还是 tx id list
            if(JsonUtil.isList(content)) {
                for (String txId : JsonUtil.str2list(content, String.class)) {
                    if (txId != null) {
                        txIdList.add(txId);
                    }
                }
            } else {
                // 此时 content 即为 tx id
                if (content != null) {
                    txIdList.add(content);
                }
            }
        }
        if (txIdList.size() > 0) {
            return genBlock(preBlockId, txIdList);
        } else {
            return null;
        }

    }

    /**
     * 从 txIdCollection 中找到还未添加到 block 中的区块
     * @param preBlockId
     * @param txIdCollection
     * @param limitSize
     * @return
     */
    public Block genBlock(String preBlockId, String txIdCollection, double limitSize){
        List<String> txIdList = MongoUtil.find(txIdCollection, limitSize);
        if (txIdList.size() > 0) {
            return this.genBlock(preBlockId, txIdList);
        } else {
            return null;
        }
    }

    /**
     * 将区块 block 保存到集合 blockChainCollection 中
     *
     * @param block
     * @param blockChainCollection
     * @return
     */
    public boolean save(Block block, String blockChainCollection) {
        String blockId = block.getBlockId();
        logger.info("开始保存区块：" + blockId);
        return blockDao.upSert(block, blockChainCollection);
//        return MongoUtil.upSertJson("blockId", block.getBlockId(), block.toString(), blockChainCollection);
    }

    /**
     * 创建并初始化 Last Block Id
     * @param collectionName
     */
    public void initLastBlockIdColl(String collectionName) {
        if (!MongoUtil.collectionExists(collectionName)) {
            logger.debug("集合" + collectionName + "不存在，开始创建");
            MongoUtil.insertKV(Const.LAST_BLOCK_ID, Const.GENESIS_BLOCK_ID, collectionName);
        }
    }

    /**
     * 创建并初始化 Last Block Id
     * @param collectionName
     */
    public void initLastBlockIdColl(MongoDB mongoDB, String collectionName) {
        if (!mongoDB.collectionExists(collectionName)) {
            logger.debug("集合" + collectionName + "不存在，开始创建");
            mongoDB.insertKV(Const.LAST_BLOCK_ID, Const.GENESIS_BLOCK_ID, collectionName);
        }
    }

    /**
     * 从 collectionName 中获取 LastBlockId
     * @param collectionName
     * @return
     */
    public String getLastBlockId(String collectionName){
        if (!MongoUtil.collectionExists(collectionName)) {
            this.initLastBlockIdColl(collectionName);
            return Const.GENESIS_BLOCK_ID;
        } else {
            String record = MongoUtil.findFirstDoc(collectionName);
            if (record != null && !record.equals("")) {
                try {
                    return (String) objectMapper.readValue(record, Map.class).get(Const.LAST_BLOCK_ID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.error("LastBlockId record 反序列化失败！");
                return null;

            } else {
                logger.error("获取 LastBlockId 失败！");
                return null;
            }
        }
    }

    /**
     * 从 collectionName 中获取 LastBlockId
     * @param collectionName
     * @return
     */
    public String getLastBlockId(MongoDB mongoDB, String collectionName){
        if (!mongoDB.collectionExists(collectionName)) {
            this.initLastBlockIdColl(collectionName);
            return Const.GENESIS_BLOCK_ID;
        } else {
            String record = mongoDB.findFirstDoc(collectionName);
            if (record != null && !record.equals("")) {
                try {
                    return (String) objectMapper.readValue(record, Map.class).get(Const.LAST_BLOCK_ID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.error("LastBlockId record 反序列化失败！");
                return null;

            } else {
                logger.error("获取 LastBlockId 失败！");
                return null;
            }
        }
    }

    /**
     * 到 collectionName 里去获取更新的LastBlockId
     *
     * @param collectionName
     * @throws Exception
     */
    public boolean updateLastBlockId(String newLastBlockId, String collectionName) {

        String oldLastBlockId = getLastBlockId(collectionName);
        return MongoUtil.updateKV(Const.LAST_BLOCK_ID, oldLastBlockId, newLastBlockId, collectionName);
    }

    /**
     * 将 last block id push到消息队列里
     * @param lastBlockId
     */
    public void addLastBlockIdToQueue(String lastBlockId) {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.LAST_BLOCK_ID_QUEUE);
        rmq.push(lastBlockId);
    }

    public String getLastBlockIdFromQueue() {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.LAST_BLOCK_ID_QUEUE);
        return rmq.pull();
    }

    /**
     * 从 collectionName 中获取所有区块
     * @param collectionName
     * @return
     */
    public List<Block> getAllBlocks(String collectionName) {
        return blockDao.findAll(collectionName);
    }

    public static void main(String[] args) {
        TransactionService txService = TransactionService.getInstance();
        BlockService blockService = BlockService.getInstance();
        List<String> txIdList = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            try {
                txIdList.add(txService.genTx(TxType.INSERT.getName(), new TxString("测试" + i)).getTxId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Block block = blockService.genBlock(Const.GENESIS_BLOCK_ID, txIdList);
        try {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(block));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
