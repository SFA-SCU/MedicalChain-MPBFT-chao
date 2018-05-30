package com.pancake.socket;


import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * blockers starter，用以启动多个 blocker 服务器
 */
public class BlockersStarter {
    private final static Logger logger = LoggerFactory.getLogger(BlockersStarter.class);

    /**
     * 根据 netAddressList 启动对应端口的 TxIdCollector
     *
     * @param netAddressList TxIdCollectorAddress 对象 list
     */
    public static void startBlockerServers(List<NetAddress> netAddressList) {
        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.
                newCachedThreadPool();
        for (NetAddress tic : netAddressList) {
            try {
                logger.info("开始启动端口为[" + tic.getPort() + "]的 Blocker Server");
                es.execute(new BlockerServer(tic));
                es.execute(new Blocker(tic));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        logger.info("验证节点终止运行");
    }

    public static void main(String[] args) {
        List<NetAddress> blockerList = JsonUtil.getBlockerAddressList(Const.BlockChainNodesFile);
        logger.info("Blocker 地址 list 为：" + blockerList);
        startBlockerServers(blockerList);
    }
}
