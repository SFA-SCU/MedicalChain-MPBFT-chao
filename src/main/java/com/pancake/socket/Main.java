package com.pancake.socket;

import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by chao on 2017/12/25.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // 1.
        startTransactionIdCollector();
        // 2.
        startServerFrontEnd();
        // 3.
        startTransactionTransmitter();
    }

    public static void startTransactionIdCollector(){
        NetAddress na = JsonUtil.getTxIdCollectorAddress(Const.BlockChainNodesFile);
        try {
            new Thread(new BlockerServer(na)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startServerFrontEnd() {
        List<NetAddress> list = JsonUtil.getValidatorAddressList(Const.BlockChainNodesFile);
        logger.info("Validator 地址 list 为：" + list);
        ValidatorsStarter.startValidators(list);
    }

    public static void startTransactionTransmitter() {
        new Thread(new TransactionTransmitter()).start();
    }
}
