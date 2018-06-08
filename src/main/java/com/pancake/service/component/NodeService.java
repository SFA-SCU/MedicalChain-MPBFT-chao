package com.pancake.service.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pancake.entity.pojo.RabbitmqServer;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import com.pancake.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chao on 2018/6/5.
 */
public class NodeService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(NodeService.class);

    private static class LazyHolder {
        private static final NodeService INSTANCE = new NodeService();
    }
    private NodeService (){}
    public static NodeService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 判断远程地址是否可以连接
     * @param netAddress
     * @return
     */
    public boolean isHostConnectable(NetAddress netAddress) {
        String host = netAddress.getIp();
        int port = netAddress.getPort();
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), Const.CONN_TIME_OUT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 返回节点信息与是否可连信息
     * @param netAddresses
     * @return
     */
    public Map<NetAddress, Boolean> nodesStatus(List<NetAddress> netAddresses) {
        Map<NetAddress, Boolean> map = new LinkedHashMap<NetAddress, Boolean>();
        for (NetAddress netAddress : netAddresses) {
            map.put(netAddress, this.isHostConnectable(netAddress));
        }
        return map;
    }

    /**
     * 获取 Validators 的可连信息
     * @return
     */
    public Map<NetAddress, Boolean> validatorsStatus() {
        List<NetAddress> netAddresses = JsonUtil.getValidatorAddressList();
        return nodesStatus(netAddresses);
    }

    /**
     * 获取 blocker 的可连信息
     * @return
     */
    public Map<NetAddress, Boolean> blockersStatus() {
        List<NetAddress> netAddresses = JsonUtil.getBlockerAddressList();
        return nodesStatus(netAddresses);
    }

    /**
     * 获取 Transaction Transmitter 的可连信息
     * @return
     */
    public Map<NetAddress, Boolean> txTransStatus() {
        RabbitmqServer rabbitmqServer = JsonUtil.getRabbitmqServer();
        NetAddress netAddress = new NetAddress(rabbitmqServer.getIp(), rabbitmqServer.getPort());
        Map<NetAddress, Boolean> map =
                new LinkedHashMap<NetAddress, Boolean>();
        map.put(netAddress, this.isHostConnectable(netAddress));
        return map;
    }
}
