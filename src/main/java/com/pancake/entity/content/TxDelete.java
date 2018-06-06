package com.pancake.entity.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2018/6/2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TxDelete extends TxContent {
    private String txId;

    public TxDelete() {
    }

    public TxDelete(String txId) {
        super("TxDelete");
        this.txId = txId;
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

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }
}
