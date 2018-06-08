package com.pancake.web;

import com.pancake.entity.component.Block;
import com.pancake.entity.util.Const;
import com.pancake.service.component.BlockService;
import com.pancake.socket.Blocker;
import com.pancake.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao on 2018/6/3.
 */
@Controller
@RequestMapping(value = "/block")
public class BlockController {
    private final static Logger logger = LoggerFactory.getLogger(BlockController.class);
    private BlockService blockService = BlockService.getInstance();

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("/block_html/index");
        return mav;
    }

    @RequestMapping(value = "/genesis", method = RequestMethod.GET)
    public ModelAndView genesis() {
        String txId = Const.GENESIS_TX_ID;
//        TxString txString = new TxString(txId);
        List<String> txIdList = new ArrayList<String>();
        txIdList.add(txId);
        Block block = blockService.genBlock(Const.GENESIS_BLOCK_ID, txIdList);
        logger.info("block: " + block);
        Blocker blocker = new Blocker();
        blocker.sendBlock(block, NetUtil.getPrimaryNode());

        ModelAndView mav = new ModelAndView("/block_html/index");
        mav.addObject("block", block);
        return mav;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search(@RequestParam("blockId") String blockId) {
        logger.info("blockId 为: " + blockId);
        ModelAndView mav = new ModelAndView("/block_html/show");
        Block block = blockService.findById(blockId);
//        logger.info("block: " + block);
        mav.addObject("block", block);
        return mav;
    }

    @RequestMapping(value = "/blockchain", method = RequestMethod.GET)
    public ModelAndView blockchain() {
        ModelAndView mav = new ModelAndView("/block_html/blockchain");
        List<Block> blockList = blockService.findAll();
        mav.addObject("blockList", blockList);
        return mav;
    }
}
