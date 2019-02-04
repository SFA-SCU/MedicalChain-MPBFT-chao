package com.pancake.entity.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.util.Const;

/**
 * Created by chao on 2017/11/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitMessage extends Message {
    private String prepareMessageId; //
    private String clientMsgId;
    private String ip;  //发送 CommitMessage 节点的ip
    private int port;  // 发送 CommitMessage 节点的端口

    public CommitMessage() {
    }

    public CommitMessage(String msgId, String timestamp, String pubKey, String signature,
                         String prepareMessageId, String clientMsgId, String ip, int port) {
        super(msgId, Const.CMTM, timestamp, pubKey, signature);
        this.prepareMessageId = prepareMessageId;
        this.clientMsgId = clientMsgId;
        this.ip = ip;
        this.port = port;
    }

    public CommitMessage(String msgId, String msgType, String timestamp, String pubKey, String signature,
                         String prepareMessageId, String clientMsgId, String ip, int port) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.prepareMessageId = prepareMessageId;
        this.clientMsgId = clientMsgId;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        String rtn = null;
        try {
            rtn = (new ObjectMapper()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public String getPrepareMessageId() {
        return prepareMessageId;
    }

    public void setPrepareMessageId(String prepareMessageId) {
        this.prepareMessageId = prepareMessageId;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
