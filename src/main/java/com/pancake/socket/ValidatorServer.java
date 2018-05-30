package com.pancake.socket;

import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.handler.CommittedMsgHandler;
import com.pancake.handler.ValidatorHandler;
import com.pancake.handler.PreparedMsgHandler;
import com.pancake.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chao on 2017/11/21.
 */
@SuppressWarnings("InfiniteLoopStatement")
public class ValidatorServer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ValidatorServer.class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private NetAddress validatorAddr;

    public ValidatorServer(NetAddress validatorAddr) throws IOException {
        this.validatorAddr = validatorAddr;
        serverSocket = new ServerSocket(validatorAddr.getPort());
        threadPool = Executors.newCachedThreadPool();
//        serverSocket.setSoTimeout(100000);
    }

    /**
     * 保留 poolSize 是为了使用 newFixedThreadPool
     *
     * @param port
     * @param poolSize
     * @throws IOException
     */
    public ValidatorServer(int port, int poolSize) throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(poolSize);
//        serverSocket.setSoTimeout(100000);
    }

    public void run() {
        try {
            String ip = validatorAddr.getIp();
            logger.info("服务器 [" + ip + ":"
                    + serverSocket.getLocalPort() + "] 启动");
            logger.info("服务器 [" + ip + ":"
                    + serverSocket.getLocalPort() + "] 启动检测生成 PreparedMessage 服务器");
            new Thread(new PreparedMsgHandler(validatorAddr)).start();
            logger.info("服务器 [" + ip + ":"
                    + serverSocket.getLocalPort() + "] 启动检测生成 CommittedMessage 服务器");
            new Thread(new CommittedMsgHandler(validatorAddr)).start();

            while (true) {
                threadPool.execute(new ValidatorHandler(serverSocket.accept(), validatorAddr));
            }
        } catch (IOException ex) {
            threadPool.shutdown();
        }

    }

    public static void main(String[] args) {
        // 单独启动一个 Validator 服务器
        NetAddress validatorAddr = JsonUtil.getCurrentValidator(Const.BlockChainNodesFile);
        try {
            Thread t = new Thread(new ValidatorServer(validatorAddr));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
