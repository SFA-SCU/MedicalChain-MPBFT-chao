package com.pancake.handler;

/**
 * Created by chao on 2017/11/21.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.service.message.impl.*;
import com.pancake.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Map;


/**
 * 用于解析 socket 中 msg 的类型，并根据不同的类型，交由不同的其他 ValidatorHandler 去处理
 */
public class ValidatorHandler implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ValidatorHandler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Socket socket;
    private CommitMessageService commitMessageService = CommitMessageService.getInstance();
    private TransactionMessageService txMsgSerice = TransactionMessageService.getInstance();
    private PrepareMessageService prepareMessageService = PrepareMessageService.getInstance();
    private NetAddress validatorAddress;
    private BlockMessageService blockMessageService = BlockMessageService.getInstance();

    public ValidatorHandler(Socket socket, NetAddress validatorAddress) {
        this.socket = socket;
        this.validatorAddress = validatorAddress;
    }

    public void run() {
        // read and service request on socket
        try {
            logger.info("请求客户端地址：" + socket.getRemoteSocketAddress());
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
//            String rcvMsg = in.readUTF();
            String rcvMsg = NetUtil.read(dataInputStream);
            String msgType = (String) objectMapper.readValue(rcvMsg, Map.class).get("msgType");
            logger.debug("接收到的 Msg 类型为： [" + msgType + "]");

            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream= new DataOutputStream(outputStream);

            // 1. 接收到来自其他节点的区块消息，验证后生成准备消息，广播
            if (msgType.equals(Const.BM)) {
                NetUtil.write(dataOutputStream, "接收到你发送 Block 消息");
                socket.shutdownOutput();
//                socket.close();

                try {
                    blockMessageService.procBlockMsg(rcvMsg, validatorAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 2. 接收到来自其他节点的交易单消息，验证后生成准备消息，广播
            else if(msgType.equals(Const.TXM)) {
                NetUtil.write(dataOutputStream, "接收到你发送的 Transaction 消息");
                socket.shutdownOutput();
//                socket.close();
                txMsgSerice.processTxMessage(rcvMsg, validatorAddress);
            }
            // 3. 接收到来自其他节点的准备消息，验证后保存
            else if (msgType.equals(Const.PM)) {
                NetUtil.write(dataOutputStream, "接收到你发送的 Prepare 消息");
                socket.shutdownOutput();
//                socket.close();
                prepareMessageService.processPrepareMessage(rcvMsg, validatorAddress);
            }
            // 4. 接收到来自其他节点的提交消息，验证后保存
            else if (msgType.equals(Const.CMTM)) {
                NetUtil.write(dataOutputStream, "接收到你发送的 Commit 消息");
                socket.shutdownOutput();
//                socket.close();
                commitMessageService.processCommitMessage(rcvMsg, validatorAddress);
            }
            else {
                NetUtil.write(dataOutputStream, "未知的 msgType 类型");
                socket.shutdownOutput();
//                socket.close();
                logger.error("未知的 msgType 类型");
            }

            // 关闭资源
            dataInputStream.close();
            dataOutputStream.close();
            inputStream.close();
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
