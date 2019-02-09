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
    private String clientMsgId;
    private int clientMsgIdCount;
    private boolean committed;

    public CommitMessageCount() {
    }

    public CommitMessageCount(String clientMsgId, int clientMsgIdCount, boolean committed) {
        this.clientMsgId = clientMsgId;
        this.clientMsgIdCount = clientMsgIdCount;
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

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public int getClientMsgIdCount() {
        return clientMsgIdCount;
    }

    public void setClientMsgIdCount(int clientMsgIdCount) {
        this.clientMsgIdCount = clientMsgIdCount;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }
}
