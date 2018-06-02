package com.pancake.util;

import com.pancake.entity.util.Const;
import org.junit.Test;


public class JsonUtilTest {
    @Test
    public void getMongoDBConfig() {
        System.out.println(JsonUtil.getMongoDBConfig(Const.BlockChainConfigFile));
    }

    @Test
    public void getCurrentValidator() {
        System.out.println(JsonUtil.getCurrentValidator(Const.BlockChainConfigFile));
    }

    @Test
    public void getCurrentBlocker() {
        System.out.println(JsonUtil.getCurrentBlocker(Const.BlockChainConfigFile));
    }
}