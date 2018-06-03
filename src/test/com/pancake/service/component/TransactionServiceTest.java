package com.pancake.service.component;

import com.pancake.entity.component.Transaction;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by chao on 2018/6/3.
 */
public class TransactionServiceTest {

    private TransactionService txSrv = TransactionService.getInstance();

    @Test
    public void findById() throws Exception {
        Transaction tx = txSrv.findById("G2Mj0xM6c+ZOw7j50Y7tdgKd396x+BSBznhuGump9bk=");
        System.out.println(tx);
    }

    @Test
    public void findById1() throws Exception {
    }

    @Test
    public void findBlockIdById() {
        String blockId = txSrv.findBlockIdById("G2Mj0xM6c+ZOw7j50Y7tdgKd396x+BSBznhuGump9bk=");
        System.out.println(blockId);
    }

}