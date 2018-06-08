package com.pancake.entity.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2018/6/2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TxUpdate extends TxContent {
    private String oldTxId;
    private String newTxId;

    public TxUpdate() {
    }

    public TxUpdate(String oldTxId, String newTxId) {
        super("TxUpdate");
        this.oldTxId = oldTxId;
        this.newTxId = newTxId;
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

    public String getOldTxId() {
        return oldTxId;
    }

    public void setOldTxId(String oldTxId) {
        this.oldTxId = oldTxId;
    }

    public String getNewTxId() {
        return newTxId;
    }

    public void setNewTxId(String newTxId) {
        this.newTxId = newTxId;
    }
}
