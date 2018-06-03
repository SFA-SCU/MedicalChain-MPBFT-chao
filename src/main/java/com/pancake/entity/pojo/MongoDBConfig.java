package com.pancake.entity.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.util.NetAddress;

/**
 * Created by chao on 2018/6/2.
 */
public class MongoDBConfig {
    private String ip;
    private int port;
    private String database;

    public MongoDBConfig() {
    }

    public MongoDBConfig(NetAddress netAddress, String database) {
        this.ip = netAddress.getIp();
        this.port = netAddress.getPort();
        this.database = database;
    }

    public MongoDBConfig(String ip, int port, String database) {
        this.ip = ip;
        this.port = port;
        this.database = database;
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
