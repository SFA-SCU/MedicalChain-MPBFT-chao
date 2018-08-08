package com.pancake.factory.impl;

import com.pancake.factory.MessageProcesserFactory;
import com.pancake.message.processer.MessageProcesser;
import com.pancake.message.processer.impl.BlockMessageProcesser;

public class BlockMessageProcesserFactory implements MessageProcesserFactory {

	public MessageProcesser createMessageProcesser() {
		return new BlockMessageProcesser();
	}

}
