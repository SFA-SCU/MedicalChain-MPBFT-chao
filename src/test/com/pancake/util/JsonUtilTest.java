package com.pancake.util;

import com.pancake.entity.util.Const;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonUtilTest {

    @Test
    public void getCurrentValidator() {
        System.out.println(JsonUtil.getCurrentValidator(Const.BlockChainNodesFile));
    }

    @Test
    public void getCurrentBlocker() {
        System.out.println(JsonUtil.getCurrentBlocker(Const.BlockChainNodesFile));
    }
}