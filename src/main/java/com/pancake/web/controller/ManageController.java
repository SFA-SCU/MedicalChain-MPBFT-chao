package com.pancake.web.controller;

import com.pancake.service.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by chao on 2017/6/13.
 */
@Controller
public class ManageController {

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


    @RequestMapping(value = "/")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("index");
    }

}
