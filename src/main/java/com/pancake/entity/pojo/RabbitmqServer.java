package com.pancake.entity.pojo;

public class RabbitmqServer {
    private String userName;
    private String password;
    private String ip;
    private int port;

    public RabbitmqServer() {
    }

    public RabbitmqServer(String userName, String password, String ip, int port) {
        this.userName = userName;
        this.password = password;
        this.ip = ip;
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
