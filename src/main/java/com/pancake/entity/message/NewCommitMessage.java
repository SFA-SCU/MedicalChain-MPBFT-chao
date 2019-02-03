package com.pancake.entity.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.util.Const;

/**
 * Created by chao on 2017/11/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewCommitMessage extends Message {
    private String prepareMessageId; //
    private String txId;
    private String ip;  //发送 CommitMessage 节点的ip
    private int port;  // 发送 CommitMessage 节点的端口

    public NewCommitMessage() {
    }

    public NewCommitMessage(String msgId, String timestamp, String pubKey, String signature,
                            String prepareMessageId, String txId, String ip, int port) {
        super(msgId, Const.CMTM, timestamp, pubKey, signature);
        this.prepareMessageId = prepareMessageId;
        this.txId = txId;
        this.ip = ip;
        this.port = port;
    }

    public NewCommitMessage(String msgId, String msgType, String timestamp, String pubKey, String signature,
                            String prepareMessageId, String txId, String ip, int port) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.prepareMessageId = prepareMessageId;
        this.txId = txId;
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

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
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
