package com.pancake.entity.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2019/2/3.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TxTransmitterConfig {
    private double limitTime;
    private double limitSize;

    public TxTransmitterConfig() {
    }

    public TxTransmitterConfig(double limitTime, double limitSize) {
        this.limitTime = limitTime;
        this.limitSize = limitSize;
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

    public double getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(double limitTime) {
        this.limitTime = limitTime;
    }

    public double getLimitSize() {
        return limitSize;
    }

    public void setLimitSize(double limitSize) {
        this.limitSize = limitSize;
    }
}
