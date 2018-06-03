package com.pancake.web;

import com.pancake.entity.component.Block;
import com.pancake.service.component.BlockService;
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
@RequestMapping(value = "/block")
public class BlockController {
    private final static Logger logger = LoggerFactory.getLogger(BlockController.class);
    private BlockService blockService = BlockService.getInstance();

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search(@RequestParam("blockId") String blockId) {
        logger.info("blockId 为: " + blockId);
        ModelAndView mav = new ModelAndView("/blockht/show");
        Block block = blockService.findById(blockId);
//        logger.info("block: " + block);
        mav.addObject("block", block);
        return mav;
    }
}
