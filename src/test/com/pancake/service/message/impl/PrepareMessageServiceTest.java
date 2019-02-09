package com.pancake.service.message.impl;

import com.pancake.entity.message.PrepareMessage;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by chao on 2019/2/9.
 */
public class PrepareMessageServiceTest {
    private final static PrepareMessageService prepareMsgService = PrepareMessageService.getInstance();
    @Test
    public void getInstance() throws Exception {
    }

    @Test
    public void processPrepareMessage() throws Exception {
    }

    @Test
    public void save() throws Exception {
    }

    @Test
    public void save1() throws Exception {
    }

    @Test
    public void genInstance() throws Exception {
    }

    @Test
    public void verify() throws Exception {
    }

    @Test
    public void getSignContent() throws Exception {
    }

    @Test
    public void getByClientMsgId() throws Exception {
        PrepareMessage prepareMessage = prepareMsgService.getByClientMsgId("D8j1LHiwnyVz7fCppreejMzPOVdbxsBW1dgQzvNKNfc=",
                "127.0.0.1:8000.PrepareMsg");
        System.out.println(prepareMessage);
    }

}