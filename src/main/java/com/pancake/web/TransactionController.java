package com.pancake.web;

import com.pancake.entity.component.Transaction;
import com.pancake.service.component.TransactionService;
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

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search(@RequestParam("txId") String txId) {
        logger.info("txId 为: " + txId);
        ModelAndView mav = new ModelAndView("/transaction/show");
        Transaction tx = txSrv.findById(txId);
        mav.addObject("tx", tx);
        return mav;
    }
}
