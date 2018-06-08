package com.pancake.web;

import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.*;
import com.pancake.entity.enumeration.TxType;
import com.pancake.service.component.TransactionService;
import com.pancake.service.content.TxDeleteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by chao on 2018/6/3.
 */
@Controller
@RequestMapping(value = "/tx")
public class TransactionController {
    private final static Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private TransactionService txSrv = TransactionService.getInstance();
    private TxDeleteService txDeleteService = new TxDeleteService();

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("/transaction/index");
        return mav;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search(@RequestParam("txId") String txId) {
        logger.info("txId 为: " + txId);

        Transaction tx = txSrv.findById(txId);
        logger.info("tx: " + tx);
        String blockId = txSrv.findBlockIdById(txId);

        ModelAndView mav = this.getMav(tx);

        String delTxId = txSrv.isDeleted(txId);
        Transaction updateTx = txSrv.isUpdated(txId);

//        logger.info("delTxId:" + delTxId);
        if (delTxId!=null) {
            mav.addObject("delTxId", delTxId);
        }

        if (updateTx!=null) {
            Transaction oldTx = txSrv.findById(((TxUpdate)updateTx.getContent()).getOldTxId());
            mav.addObject("oldTx", oldTx);
            mav.addObject("updateTx", updateTx);
        }

        mav.addObject("tx", tx);
        mav.addObject("blockId", blockId);

        return mav;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ModelAndView delete(@RequestParam("txId") String txId) {
        logger.info("删除的txid为：" + txId);
        ModelAndView mav = new ModelAndView("transaction/show_delete");
        TxDelete txDeleteObj = new TxDelete(txId);
        String delTxId = txDeleteService.save(txDeleteObj);
        logger.info("delTxId: " + delTxId);
//        Transaction delTx = txSrv.findById(delTxId);
//        String blockId = txSrv.findBlockIdById(delTxId);
        return mav;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ModelAndView update(Transaction tx, Record record) {
        String oldTxId = tx.getTxId();
        logger.info("被更新的txid为：" + oldTxId);
        record.setContentType(record.getClass().getSimpleName());
        Transaction oldTx = txSrv.findById(oldTxId);
        Transaction newTx = txSrv.save(record, TxType.INSERT);
        TxUpdate txUpdate = new TxUpdate(oldTxId, newTx.getTxId());
        Transaction updateTx = txSrv.save(txUpdate, TxType.UPDATE);

        ModelAndView mav = this.getMav(newTx);

        mav.addObject("oldTx", oldTx);
        logger.info("oldTx: " + tx);
        mav.addObject("tx", newTx);
        mav.addObject("updateTx", updateTx);
//        TxDelete txDeleteObj = new TxDelete(txId);
//        String delTxId = txDeleteService.save(txDeleteObj);
//        logger.info("delTxId: " + delTxId);
        return mav;
    }

    private ModelAndView getMav(Transaction tx) {
        if (tx.getContent().getContentType().equals(Record.class.getSimpleName())) {
            return new ModelAndView("transaction/show_record");
        } else if (tx.getContent().getContentType().equals(TxString.class.getSimpleName())) {
            return new ModelAndView("transaction/show_string");
        } else if (tx.getContent().getContentType().equals(TxDelete.class.getSimpleName())) {
            return new ModelAndView("transaction/show_delete");
        } else if (tx.getContent().getContentType().equals(TxUpdate.class.getSimpleName())) {
            return new ModelAndView("transaction/show_update");
        } else {
            return new ModelAndView("transaction/error");
        }
    }
}
