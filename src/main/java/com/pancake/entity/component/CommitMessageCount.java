package com.pancake.entity.component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2019/2/3.
 * 统计 commit msg 的数量，达到 2f 个后提交交易单
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitMessageCount {
    private String txId;
    private int txIdCount;
    private boolean committed;

    public CommitMessageCount() {
    }

    public CommitMessageCount(String txId, int txIdCount, boolean committed) {
        this.txId = txId;
        this.txIdCount = txIdCount;
        this.committed = committed;
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

    public int getTxIdCount() {
        return txIdCount;
    }

    public void setTxIdCount(int txIdCount) {
        this.txIdCount = txIdCount;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }
}
