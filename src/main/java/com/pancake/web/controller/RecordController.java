package com.pancake.web.controller;

import com.pancake.entity.pojo.InfectiousDiseaseReportCard;
import com.pancake.service.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by chao on 2017/6/13.
 */
@Controller
@RequestMapping("/record")
public class RecordController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PatientBelongService patientBelongService;
    @Autowired
    private PatientCareerService patientCareerService;
    @Autowired
    private CaseClassificationService caseClassificationService;
    @Autowired
    private InfectiousDiseaseService infectiousDiseaseService;
    @Autowired
    private InfectiousDiseaseReportCardService infectiousDiseaseReportCardService;

    /**
     * 跳转到添加页面
     * @return Result 对象
     */
    @RequestMapping(value = "add", method = RequestMethod.GET)
    public ModelAndView add() {
        ModelAndView mav = new ModelAndView("add_record");
        try {
            mav.addObject("patientBelongList", patientBelongService.getAll());
            mav.addObject("patientCareerList", patientCareerService.getAll());
            mav.addObject("caseClassificationList", caseClassificationService.getAll());
            mav.addObject("infectiousDiseaseList", infectiousDiseaseService.getAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mav;
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ModelAndView save(InfectiousDiseaseReportCard card) {
        logger.info("card info : " + card.toString());
        infectiousDiseaseReportCardService.saveInBlock(card);
        ModelAndView mav = new ModelAndView("save_success");
        return mav;
    }

    @RequestMapping(value = "query", method = RequestMethod.GET)
    public ModelAndView query(@RequestParam("reportCardId") String reportCardId) {
        logger.info("查询报告卡：" + reportCardId);
        ModelAndView mav = new ModelAndView("save_success");
        return mav;
    }
}
