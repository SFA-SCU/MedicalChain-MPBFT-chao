package com.pancake.util;

import com.pancake.entity.enumeration.TxType;
import com.pancake.entity.util.Const;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by chao on 2018/6/3.
 */
public class MongoDBTest {

    private MongoDB mongoDB = new MongoDB(JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile));

    @Test
    public void find() throws Exception {
        String collection = "127.0.0.1:8000.BlockChain";
        List<String> list = mongoDB.find("txIdList", "URRyDFQSmTa7nA2uS4/Gpxms9GhEP5Ygc6Xv/iu43xM=",
                collection);
        System.out.println(list);
    }

    @Test
    public void findByKVs() {
        String collection = "127.0.0.1:8000.Transaction";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("txType", TxType.DELETE.getName());
        map.put("content.txId", "G2Mj0xM6c+ZOw7j50Y7tdgKd396x+BSBznhuGump9bk=");
        List<String> list = mongoDB.findByKVs(map, collection);
        System.out.println(list);
    }

}