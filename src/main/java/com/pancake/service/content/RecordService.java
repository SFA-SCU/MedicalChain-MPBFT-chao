package com.pancake.service.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.Record;
import com.pancake.entity.enumeration.TxType;
import com.pancake.entity.util.Const;
import com.pancake.service.component.TransactionService;
import com.pancake.util.RabbitmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by chao on 2018/6/3.
 */
public class RecordService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(RecordService.class);
    private TransactionService txService;
    private RabbitmqUtil rmq;

    public RecordService() {
        txService = TransactionService.getInstance();
        URL configFile = RecordService.class.getClassLoader().getResource("blockchain-config-test.json");
        logger.info("configFile.getPath(): " + configFile.getPath());
        rmq = new RabbitmqUtil(Const.TX_QUEUE, configFile.getPath());
    }

    public void save(Record record) {
        // 若 record 的类型没有设置，则设为类名
        if(record.getContentType() == null || record.getContentType().trim().equals("")) {
            record.setContentType(record.getClass().getSimpleName());
        }

        Transaction tx = null;
        try {
            tx = txService.genTx(TxType.INSERT.getName(), record);
            logger.info("生成交易单: " + tx.getTxId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tx != null) {
            rmq.push(tx.toString());
        } else {
            logger.error("tx 为 null");
        }
    }
}
