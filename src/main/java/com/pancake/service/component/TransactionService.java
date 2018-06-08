package com.pancake.service.component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.dao.TransactionDao;
import com.pancake.entity.component.Block;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.TxContent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chao on 2017/11/17.
 */
public class TransactionService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private TransactionDao txDao = TransactionDao.getInstance();
    private RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE, Const.BlockChainConfigFile);

    private static class LazyHolder {
        private static final TransactionService INSTANCE = new TransactionService();
    }

    private TransactionService() {
    }

    public static TransactionService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 根据 txContent 生成交易单，并将交易单存储在区块中
     * @param txContent
     * @return 返回交易单ID
     */
    public Transaction save(TxContent txContent, TxType txType) {
        // 若 txContent 的类型没有设置，则设为类名
        if(txContent.getContentType() == null || txContent.getContentType().trim().equals("")) {
            txContent.setContentType(txContent.getClass().getSimpleName());
        }

        Transaction tx = null;
        try {
            tx = this.genTx(txType.getName(), txContent);
            logger.info("生成交易单: " + tx.getTxId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tx != null) {
            rmq.push(tx.toString());
        } else {
            logger.error("tx 为 null");
        }

        if(tx == null) {
            return null;
        }
        return tx;
    }

    /**
     * 到配置文件中的mongodb查询主节点中的 tx 是否被删除了
     * @param txId
     * @return
     */
    public String isDeleted(String txId) {
        return this.isDeleted(txId, JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile), NetUtil.getPrimaryNode());
    }

    /**
     * 查看id 为 txId 的交易单是否被删除了，若是，返回删除该交易单的交易单的ID
     * @param txId
     * @param mongoDBConfig
     * @param netAddress
     * @return
     */
    public String isDeleted(String txId, MongoDBConfig mongoDBConfig, NetAddress netAddress) {
        MongoDB mongoDB = new MongoDB(mongoDBConfig);
        String txCollection = netAddress + "." + Const.TX;

        // 查询条件
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("txType", TxType.DELETE.getName());
        map.put("content.txId", txId);

        List<String> list = mongoDB.findByKVs(map, txCollection);

        if (list == null || list.size() == 0) {
            return null;
        } else {
            String delTxId = null;
            try {
                delTxId = objectMapper.readValue(list.get(list.size()-1), Transaction.class).getTxId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return delTxId;
        }

    }

    /**
     * 判断 tx 是否为被修改过的 tx
     * @param txId
     * @return
     */
    public Transaction isUpdated(String txId) {
        return this.isUpdated(txId, JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile), NetUtil.getPrimaryNode());
    }

    /**
     * 判断 tx 是否为被修改过的 tx
     * @param txId
     * @param mongoDBConfig
     * @param netAddress
     * @return
     */
    public Transaction isUpdated(String txId, MongoDBConfig mongoDBConfig, NetAddress netAddress) {
        MongoDB mongoDB = new MongoDB(mongoDBConfig);
        String txCollection = netAddress + "." + Const.TX;

        // 查询条件
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("txType", TxType.UPDATE.getName());
        map.put("content.newTxId", txId);

        List<String> list = mongoDB.findByKVs(map, txCollection);

        if (list == null || list.size() == 0) {
            return null;
        } else {
            Transaction updatedTx = null;
            try {
                updatedTx = objectMapper.readValue(list.get(list.size()-1), Transaction.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return updatedTx;
        }
    }

    /**
     * 到配置文件中的mongodb查询主节点中的 tx
     * @param txId
     * @return
     */
    public Transaction findById(String txId) {
        return this.findById(txId, JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile), NetUtil.getPrimaryNode());
    }

    /**
     * 根据 TxId 到指定 mongodb 获取 tx
     * @param txId
     * @param mongoDBConfig
     * @return
     */
    public Transaction findById(String txId, MongoDBConfig mongoDBConfig, NetAddress netAddress) {
        MongoDB mongoDB = new MongoDB(mongoDBConfig);
        String txCollection = netAddress + "." + Const.TX;
        List<String> list = mongoDB.find("txId", txId, txCollection);
        if (list.size() == 0)
            return null;
        else if (list.size() == 1) {
            Transaction tx = null;
            try {
                tx = objectMapper.readValue(list.get(0), Transaction.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return tx;
        } else {
            logger.error("id 为： " + txId + " 的tx记录存在多条");
            return null;
        }
    }

    /**
     * 到配置文件中的mongodb查询主节点中的 block
     * @param txId
     * @return
     */
    public Block findBlockById(String txId) {
        return this.findBlockById(txId, JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile), NetUtil.getPrimaryNode());
    }

    /**
     * 到配置文件中的mongodb查询主节点中的 block id
     * @param txId
     * @return
     */
    public String findBlockIdById(String txId) {
        Block block = this.findBlockById(txId, JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile),
                NetUtil.getPrimaryNode());
        return block.getBlockId();
    }

    /**
     * 根据 TxId 到指定 mongodb 获取 block
     * @param txId
     * @param mongoDBConfig
     * @return
     */
    public Block findBlockById(String txId, MongoDBConfig mongoDBConfig, NetAddress netAddress) {
        MongoDB mongoDB = new MongoDB(mongoDBConfig);
        String txCollection = netAddress + "." + Const.BLOCK_CHAIN;
        // "txIdList", txId 是查询数组的方式
        List<String> list = mongoDB.find("txIdList", txId, txCollection);
        //noinspection Duplicates
        if (list.size() == 0)
            return null;
        else if (list.size() == 1) {
            Block block = null;
            try {
                block  = objectMapper.readValue(list.get(0), Block.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return block ;
        } else {
            Block block = null;
            try {
                block  = objectMapper.readValue(list.get(list.size()-1), Block.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.error("id 为： " + txId + " 的block记录存在多条");
            return block ;
        }
    }


    /**
     * 根据 id 判断 tx 是否存在于集合 collectionName 中
     *
     * @param txId
     * @param collectionName
     * @return
     */
    public boolean exited(String txId, String collectionName) {
        return txDao.existed("txId", txId, collectionName);
    }

    /**
     * 判断 td id list 中 id 是否已被保存在集合当中
     *
     * @param txIdList
     * @param collection
     * @return
     */
    public boolean allExited(List<String> txIdList, String collection) {
        for (String txId : txIdList) {
            if (!this.exited(txId, collection)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据 Transaction 的类型，和要存储在 Transaction 中的 content 来生成一个 Transaction 对象
     *
     * @param txType
     * @param content
     * @return
     * @throws Exception
     */
    public Transaction genTx(String txType, TxContent content) throws Exception {
        if (content == null) {
            logger.error("content内容为null");
            throw new Exception("content内容为null");
        }

        PrivateKey privateKey = SignatureUtil.loadPvtKey("EC");
        String pubKey = SignatureUtil.loadPubKeyStr("EC");
        String timestamp = TimeUtil.getNowTimeStamp();
        String sigContent = txType + content + timestamp;
        String signature = SignatureUtil.sign(privateKey, sigContent);
        String txId = SignatureUtil.getSha256Base64(signature);

        return new Transaction(txId, signature, txType, pubKey, content, timestamp);
    }

    /**
     * 根据 Transaction json 字符串生成 Transaction 对象
     *
     * @param txJson
     * @return
     */
    public static Transaction genTx(String txJson) {
        try {
            return objectMapper.readValue(txJson, Transaction.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从消息队列中获取 Transaction，失败则返回null
     *
     * @param queueName
     * @return
     */
    public Transaction pullTx(String queueName) {
        return txDao.pull(queueName);
    }

    /**
     * 根据时间大小限制获取 Transaction
     *
     * @param queueName
     * @param limitTime
     * @param limitSize
     * @return
     */
    public List<Transaction> pullTxList(String queueName, double limitTime, double limitSize) {
        return txDao.pull(queueName, limitTime, limitSize);
    }

    public void pushTx(Transaction tx, String queueName) {
        txDao.push(tx, queueName);
    }

    /**
     * 解析 tx json list
     *
     * @param txListJson
     * @return
     */
    public List<Transaction> genTxList(String txListJson) {
        List<Transaction> txList = new ArrayList<Transaction>();
        try {
            JavaType javaType = JsonUtil.getCollectionType(ArrayList.class, Transaction.class);
            txList = objectMapper.readValue(txListJson, javaType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txList;
    }


    /**
     * 获取 Transaction List 的所有 id
     *
     * @param txListJson
     * @return
     */
    public List<String> getTxIdList(String txListJson) {
        return this.getTxIdList(this.genTxList(txListJson));
    }

    public List<String> getTxIdList(List<Transaction> list) {
        List<String> txIdList = new ArrayList<String>();
        for (Transaction tx : list) {
            txIdList.add(tx.getTxId());
        }
        return txIdList;
    }

    public String getTxId(String txJson) {
        Transaction tx = genTx(txJson);
        if (tx != null) {
            return tx.getTxId();
        } else {
            return null;
        }
    }

    /**
     * 将区块 tx保存到集合 txCollection 中
     *
     * @param tx
     * @param txCollection
     * @return
     */
    public boolean save(Transaction tx, String txCollection) {
        return txDao.upSert(tx, txCollection);
    }

    public boolean saveBatch(List<Transaction> txList, String txCollection) {
        return txDao.upSertBatch(txList, txCollection);
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(genTx("string", "测试"));
        String mapStr = "{\"age\":30}";
        genTx(mapStr);
    }
}
