package com.pancake.web;

import com.pancake.entity.component.Transaction;
import com.pancake.entity.content.Record;
import com.pancake.entity.content.TxDelete;
import com.pancake.entity.content.TxString;
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
        ModelAndView mav = null;
        Transaction tx = txSrv.findById(txId);
        String blockId = txSrv.findBlockIdById(txId);
        if (tx.getContent().getContentType().equals(Record.class.getSimpleName())) {
            mav = new ModelAndView("transaction/show_record");
        } else if (tx.getContent().getContentType().equals(TxString.class.getSimpleName())) {
            mav = new ModelAndView("transaction/show_string");
        } else if (tx.getContent().getContentType().equals(TxDelete.class.getSimpleName())) {
            mav = new ModelAndView("transaction/show_delete");
        } else {
            mav = new ModelAndView("transaction/error");
        }

        mav.addObject("tx", tx);
        mav.addObject("blockId", blockId);

        String delTxId = txSrv.isDeleted(txId);
//        logger.info("delTxId:" + delTxId);
        if (delTxId!=null) {
            mav.addObject("delTxId", delTxId);
        }
        return mav;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ModelAndView delete(@RequestParam("txId") String txId) {
        logger.info("删除的txid为：" + txId);
        ModelAndView mav = new ModelAndView("index");
        TxDelete txDeleteObj = new TxDelete(txId);
        String delTxId = txDeleteService.save(txDeleteObj);
        logger.info("delTxId: " + delTxId);
        return mav;
    }
}
