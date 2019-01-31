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

    @Test
    public void getValidatorMongoAddr() {
        System.out.println(JsonUtil.getValidatorMongoAddr(Const.BlockChainConfigFile));
    }
    @Test
    public void testBlockSize() {
        System.out.println(JsonUtil.getBlockSize(Const.BlockChainConfigFile));
    }
    @Test
    public void testTimeInterval() {
        System.out.println(JsonUtil.getTimeInterval(Const.BlockChainConfigFile));
    }
}