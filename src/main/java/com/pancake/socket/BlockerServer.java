package com.pancake.socket;


import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.handler.BlockerServerHandler;
import com.pancake.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chao on 2017/12/25.
 * blocker server 服务器，用以接收 Validator 发送来的消息并进行处理
 */
public class BlockerServer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(BlockerServer.class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private NetAddress netAddr;  // BlockerServer 的网络地址

    public BlockerServer(NetAddress netAddr) throws IOException {
        this.netAddr = netAddr;
        this.serverSocket = new ServerSocket(netAddr.getPort());
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void run() {
        try {
            logger.info("启动 BlockerServer 服务器 " + netAddr);
            while (true) {
                threadPool.execute(new BlockerServerHandler(serverSocket.accept(), netAddr));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 同时启动 Blocker 与 BlockerServer服务器
     * @param netAddr
     */
    public static void start(NetAddress netAddr) {
        try {
            Thread tBlockerServer = new Thread(new BlockerServer(netAddr));
            Thread tBlocker = new Thread(new Blocker(netAddr));
            // 1. 启动 BlockerServer，用于接收来自 Validator 的消息
            tBlockerServer.start();
            // 2. 启动 Blocker，用于从 RabbitMQ 中获取 Transaction Id，打包成区块，发送到 Validator 主节点上
            tBlocker.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        NetAddress netAddr = JsonUtil.getCurrentBlocker(Const.BlockChainNodesFile);
        start(netAddr);

    }
}
