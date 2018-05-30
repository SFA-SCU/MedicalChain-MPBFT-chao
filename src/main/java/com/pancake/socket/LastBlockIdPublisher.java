package com.pancake.socket;

import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.handler.LastBlockIdPublisherHandler;
import com.pancake.service.component.impl.BlockService;
import com.pancake.util.JsonUtil;
import com.pancake.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by chao on 2017/12/19.
 * 用做 Pre Block Id 的发布中心
 */
public class LastBlockIdPublisher implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(LastBlockIdPublisher.class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private BlockService blockService = BlockService.getInstance();

    public LastBlockIdPublisher(int port, int poolSize) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void run() {
//        NetAddress na = JsonUtil.getPublisherAddress(Const.BlockChainNodesFile);
//        NettyServer publisherServer = new NettyServer(na.getPort(), new PublisherHandler());
//        try {
//            logger.info("启动 PreBlockPublisher 服务器");
//            publisherServer.start();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String url = NetUtil.getRealIp() + ":" + serverSocket.getLocalPort();
        String lbiCollection = "Publisher" + url + "." + Const.LAST_BLOCK_ID;
        String lastBlockId = blockService.getLastBlockId(lbiCollection);
        blockService.addLastBlockIdToQueue(lastBlockId);

        try {
            logger.info("启动 PreBlockPublisher 服务器");
            //noinspection InfiniteLoopStatement
            while (true) {
                threadPool.execute(new LastBlockIdPublisherHandler(serverSocket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        NetAddress na = JsonUtil.getPublisherAddress(Const.BlockChainNodesFile);
        try {
            new Thread(new LastBlockIdPublisher(na.getPort(), availableProcessors)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
