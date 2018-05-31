package com.pancake.socket;

import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.JsonUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by chao on 2017/12/25.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Create a Parser
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("b", "blocker", true,
                "Start blocker according to the config file");
        options.addOption("v", "validator", true,
                "Start validator according to the config file");
        options.addOption("t", "transaction transmitter", true,
                "Start transaction transmitter according to the config file");

        // Parse the program arguments
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Set the appropriate variables based on supplied options
        boolean verbose = false;
        String optValue = "";

        if (commandLine.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("Options", options);
        }
        if (commandLine.hasOption('b')) {
            optValue = commandLine.getOptionValue('b');
            logger.info("配置文件位置: " + optValue);
            Const.BlockChainConfigFile = optValue;
            startBlockerServer();
        }
        if (commandLine.hasOption('v')) {
            optValue = commandLine.getOptionValue('v');
            logger.info("配置文件位置: " + optValue);
            Const.BlockChainConfigFile = optValue;
            startValidatorServer();
        }
        if(commandLine.hasOption('t')) {
            optValue = commandLine.getOptionValue('t');
            logger.info("配置文件位置: " + optValue);
            Const.BlockChainConfigFile = optValue;
            startTransactionTransmitter();
        }
    }

    public static void startBlockerServer() {
        logger.info("pvt_key: " + JsonUtil.getPvtKeyFile(Const.BlockChainConfigFile));
        logger.info("pub_key: " + JsonUtil.getPubKeyFile(Const.BlockChainConfigFile));
        NetAddress blockerAddr = JsonUtil.getCurrentBlocker(Const.BlockChainConfigFile);
        logger.info("启动 Blocker 服务器: [ " + blockerAddr + " ]");
        BlockerServer.start(blockerAddr);
    }

    public static void startValidatorServer() {
        logger.info("pvt_key: " + JsonUtil.getPvtKeyFile(Const.BlockChainConfigFile));
        logger.info("pub_key: " + JsonUtil.getPubKeyFile(Const.BlockChainConfigFile));
        NetAddress validatorAddr = JsonUtil.getCurrentValidator(Const.BlockChainConfigFile);
        logger.info("启动 Validator 服务器: [ " + validatorAddr + " ]");
        try {
            Thread t = new Thread(new ValidatorServer(validatorAddr));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startTransactionTransmitter() {
        logger.info("pvt_key: " + JsonUtil.getPvtKeyFile(Const.BlockChainConfigFile));
        logger.info("pub_key: " + JsonUtil.getPubKeyFile(Const.BlockChainConfigFile));
        logger.info("启动 TransactionTransmitter 服务器");
        new Thread(new TransactionTransmitter()).start();
    }
}
