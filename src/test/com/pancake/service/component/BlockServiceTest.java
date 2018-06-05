package com.pancake.service.component;

import com.pancake.entity.component.Block;
import com.pancake.entity.pojo.MongoDBConfig;
import com.pancake.entity.util.Const;
import com.pancake.entity.util.NetAddress;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chao on 2018/6/4.
 */
public class BlockServiceTest {
    private BlockService blockService = BlockService.getInstance();

    @Test
    public void findAll() throws Exception {
        MongoDBConfig mongoDBConfig = new MongoDBConfig("127.0.0.1", 27017, Const.BLOCK_CHAIN);
        NetAddress netAddress = new NetAddress("127.0.0.1", 8000);
        List<Block> blockList = blockService.findAll(mongoDBConfig, netAddress);
        for(Block block : blockList) {
            System.out.println(block.getBlockId() + "\n" + block.getPreBlockId() + "\n\n");
        }
    }

    @Test
    public void findAll2() throws Exception {
        List<Block> blockList = blockService.findAll();
        for(Block block : blockList) {
            System.out.println(block.getBlockId() + "\n" + block.getPreBlockId() + "\n\n");
        }
    }

}