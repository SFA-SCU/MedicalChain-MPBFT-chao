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
 * Created by chao on 2017/11/10.
 * validators starter，用以启动多个 validator 服务器
 */
public class ValidatorsStarter {

    private final static Logger logger = LoggerFactory.getLogger(ValidatorsStarter.class);

    /**
     * 根据 netAddressList 启动对应端口的 Validator
     * @param netAddressList validatorAddress 对象 list
     */
    public static void startValidators(List<NetAddress> netAddressList) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        logger.info("当前节点可用处理器数量为：" + availableProcessors);

        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.
                newCachedThreadPool();
        for (NetAddress netAddress : netAddressList) {
            try {
                es.execute(new ValidatorServer(netAddress));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        List<NetAddress> list = JsonUtil.getValidatorAddressList(Const.BlockChainConfigFile);
        logger.info("Validator 地址 list 为：" + list);

        startValidators(list);
    }
}
