package com.pancake.service.pojo.impl;

import com.pancake.entity.component.Transaction;
import com.pancake.entity.pojo.InfectiousDiseaseReportCard;
import com.pancake.entity.util.Const;
import com.pancake.service.component.impl.TransactionService;
import com.pancake.service.pojo.InfectiousDiseaseReportCardService;
import com.pancake.util.RabbitmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InfectiousDiseaseReportCardServiceImpl implements InfectiousDiseaseReportCardService{
    private RabbitmqUtil rmq;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public InfectiousDiseaseReportCardServiceImpl() {
        rmq = new RabbitmqUtil(Const.TX_QUEUE);
    }

    public void saveInBlock(InfectiousDiseaseReportCard infectiousDiseaseReportCard) {

        Transaction tx;
        try {
            tx = TransactionService.genTx("string", infectiousDiseaseReportCard.toString());
            logger.info("tx: " + tx);

            rmq.push(tx.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
