package com.pancake.web;

import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Created by chao on 2018/6/5.
 */
@Controller
@RequestMapping(value = "/node")
public class NodeController {
    private final static Logger logger = LoggerFactory.getLogger(NodeController.class);
    private NodeService nodeService = NodeService.getInstance();

    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public ModelAndView show() {
        ModelAndView mav = new ModelAndView("/node_html/show");
        Map<NetAddress, Boolean> validatorsStatus = nodeService.validatorsStatus();
        Map<NetAddress, Boolean> blockersStatus = nodeService.blockersStatus();

        mav.addObject("validatorsStatus", validatorsStatus);
        mav.addObject("blockersStatus", blockersStatus);
        return mav;
    }
}
