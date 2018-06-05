package com.pancake.service.component;

import org.junit.Test;

/**
 * Created by chao on 2018/6/5.
 */
public class NodeServiceTest {
    private NodeService nodeService = NodeService.getInstance();

    @Test
    public void validatorsStatus() throws Exception {
        System.out.println(nodeService.validatorsStatus());
    }

    @Test
    public void blockersStatus() throws Exception {
        System.out.println(nodeService.blockersStatus());
    }

}