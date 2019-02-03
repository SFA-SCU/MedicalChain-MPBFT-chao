package com.pancake.entity.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.util.Const;

/**
 * Created by chao on 2017/11/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrepareMessage extends Message{
    private ClientMessage clientMsg;

    public PrepareMessage() {
    }

    public PrepareMessage(ClientMessage clientMsg) {
        this.clientMsg = clientMsg;
    }

    public PrepareMessage(String msgId, String timestamp, String pubKey, String signature, ClientMessage clientMsg) {
        super(msgId, Const.PM, timestamp, pubKey, signature);
        this.clientMsg = clientMsg;
    }

    public PrepareMessage(String msgId, String msgType, String timestamp, String pubKey, String signature,
                          ClientMessage clientMsg) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.clientMsg = clientMsg;
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

    public ClientMessage getClientMsg() {
        return clientMsg;
    }

    public void setClientMsg(ClientMessage clientMsg) {
        this.clientMsg = clientMsg;
    }
}
