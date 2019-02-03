package com.pancake.socket;


import com.pancake.entity.component.Block;
import com.pancake.entity.message.BlockMessage;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.component.BlockService;
import com.pancake.service.component.BlockerService;
import com.pancake.service.component.NetService;
import com.pancake.service.message.impl.BlockMessageService;
import com.pancake.util.JsonUtil;
import com.pancake.util.MongoUtil;
import com.pancake.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chao on 2017/12/10.
 * 用于从 RabbitMQ 中获取 Transaction Id，打包成区块，发送到 Validator 主节点上
 */
public class Blocker implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Blocker.class);
    private BlockerService blockerService = BlockerService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    private NetService netService = NetService.getInstance();
    private BlockMessageService blockMessageService = BlockMessageService.getInstance();

    private long timeInterval; //生成区块并发送的频率
    private double blockSize;
    private int timeout; // Blocker 连接 Validator 的超时时间

    private NetAddress netAddr; // Blocker 的网络地址

    public Blocker() {
//        this.timeInterval = 5000;
//        this.blockSize = Const.TX_ID_LIST_SIZE;
        this.init();
        this.timeout = 5000;
    }

    public Blocker(NetAddress netAddr) {
//        this.timeInterval = 5000;
//        this.blockSize = Const.TX_ID_LIST_SIZE;
        this.init();
        this.timeout = 5000;
        this.netAddr = netAddr;
    }

    public void init(){
        this.timeInterval = JsonUtil.getTimeInterval(Const.BlockChainConfigFile);
        this.blockSize = JsonUtil.getBlockSize(Const.BlockChainConfigFile);
    }

    public void run() {
        logger.info("Blocker [" + netAddr + "] 启动");
        String lastBlockId;
        Block block;
        long blockLength;

        String blockChainCollection = netAddr + "." + Const.BLOCK_CHAIN;
        String lbiCollection = netAddr + "." + Const.LAST_BLOCK_ID;
        String txIdCollection = netAddr + "." + Const.TX_ID;

        while (true) {
            blockLength = MongoUtil.countRecords(blockChainCollection);
            if (blockLength > 0) {
                if (blockerService.isCurrentBlocker(netAddr, blockLength)) {
                    logger.info("Blocker [" + netAddr + "] 开始生成区块，当前区块链长度为" + blockLength);
                    lastBlockId = blockService.getLastBlockId(lbiCollection);
                    block = blockService.genBlock(lastBlockId, txIdCollection, this.blockSize);
                    if (block != null) {
                        logger.info("生产成区块： " + block.getBlockId());
                        this.sendBlock(block, NetUtil.getPrimaryNode());
                    } else {
                        logger.info("目前没有可以打包的TxId");
                    }
                }

            } else {
                logger.info("区块链长度为" + blockLength);
            }

            try {
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送区块 block 到 netAddr 主机
     * @param block
     * @param netAddr
     */
    public void sendBlock(Block block, NetAddress netAddr) {
        logger.info("开始向 [" + netAddr.getIp() + ":" + netAddr.getPort() + "] 发送 block: " + block.getBlockId());
        BlockMessage blockMessage = blockMessageService.genInstance(block);
        logger.info("blockMessage in send block: " + blockMessage);
        String rcvMsg = netService.sendMsg(blockMessage.toString(), netAddr.getIp(),
                netAddr.getPort(), Const.TIME_OUT);
        logger.info("服务器响应： " + rcvMsg);
    }

    /**
     * 根据 netAddressList 启动对应端口的 TxIdCollector
     * @param netAddressList TxIdCollectorAddress 对象 list
     */
    public static void startBlockers(List<NetAddress> netAddressList) {
        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.
                newCachedThreadPool();

        for (NetAddress tic : netAddressList) {
            logger.info("开始启动端口为[" + tic.getPort() + "]的 Blocker");
            es.execute(new Blocker(tic));
        }

//        logger.info("验证节点终止运行");
    }

    public static void main(String[] args) {
        List<NetAddress> blockerList = JsonUtil.getBlockerAddressList(Const.BlockChainConfigFile);
        logger.info("Blocker 地址 list 为：" + blockerList);
        startBlockers(blockerList);
    }
}
