package com.pancake.util;


import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;

import java.util.List;


/**
 * Created by chao on 2017/11/30.
 */
public class PeerUtil {
    private static List<NetAddress> list = JsonUtil.getValidatorAddressList(Const.BlockChainNodesFile);
    private static int peerCount = 0;
    private static int faultCount = 0;

    static {
        peerCount = list.size();
        faultCount = (peerCount - 1) / 3;
    }

    public static List<NetAddress> getList() {
        return list;
    }

    public static void setList(List<NetAddress> list) {
        PeerUtil.list = list;
    }

    public static int getPeerCount() {
        return peerCount;
    }

    public static void setPeerCount(int peerCount) {
        PeerUtil.peerCount = peerCount;
    }

    public static int getFaultCount() {
        return faultCount;
    }

    public static void setFaultCount(int faultCount) {
        PeerUtil.faultCount = faultCount;
    }
}
