package com.pancake.web;

import com.pancake.entity.content.Record;
import com.pancake.service.content.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by chao on 2018/6/3.
 */
@Controller
@RequestMapping(value = "/record")
public class RecordController {
    private final static Logger logger = LoggerFactory.getLogger(RecordController.class);

    @RequestMapping(value = "/add")
    public ModelAndView add(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("record/add");
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ModelAndView save(Record record) {
        logger.info("record:" + record);
        RecordService recordService = new RecordService();
        String txId = recordService.save(record);

        ModelAndView mav = new ModelAndView("record/add");
        mav.addObject("txId", txId);
        return mav;
    }
}
