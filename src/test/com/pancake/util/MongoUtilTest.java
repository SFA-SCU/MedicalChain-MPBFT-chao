package com.pancake.util;

import com.pancake.entity.pojo.MongoDBConfig;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chao on 2019/2/14.
 */
public class MongoUtilTest {
    @Test
    public void getCollection() throws Exception {
        String ip = "134.175.208.12";
        String url = ip + ":" + 8000;
        int mongoPort = 27017;
        String username = "blockchain";
        String password = "zc-12332145";
        String database = "BlockChain";

        String commitMsgCountCollection;
        MongoDBConfig config = new MongoDBConfig(ip, mongoPort, username, password, database);
        MongoDB mongoDB = new MongoDB(config);
        commitMsgCountCollection = url + "." + Const.CMTM_COUNT;
        List<String> result = mongoDB.find("committed", false, "clientMsgIdCount", 4, commitMsgCountCollection);
        for (String str : result) {
            System.out.println(commitMsgCountCollection + ": " + str);
        }
    }

}