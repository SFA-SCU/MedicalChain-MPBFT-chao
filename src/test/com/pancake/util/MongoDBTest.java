package com.pancake.util;

import com.pancake.entity.util.Const;
import org.junit.Test;

import java.util.List;

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

}