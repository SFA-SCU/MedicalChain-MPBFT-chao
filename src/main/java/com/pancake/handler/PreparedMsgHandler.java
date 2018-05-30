package com.pancake.handler;

import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.message.impl.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chao on 2017/12/5.
 */
public class PreparedMsgHandler implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(PreparedMsgHandler.class);
    private NetAddress addr;
    private MessageService msgService = new MessageService();

    public PreparedMsgHandler(NetAddress addr) {
        this.addr = addr;
    }

    public void run() {

        String url = addr.toString();
        String ppmCollection = url + "." + Const.PPM;
        String pmCollection = url + "." + Const.PM;
        String pdmCollection = url + "." + Const.PDM;
        msgService.traversePPMAndSaveMsg(ppmCollection, pmCollection, pdmCollection, Const.PDM, addr);
    }
}
