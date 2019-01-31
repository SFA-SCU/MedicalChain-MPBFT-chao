package com.pancake.factory.impl;

import com.pancake.factory.MessageProcesserFactory;
import com.pancake.message.processer.MessageProcesser;
import com.pancake.message.processer.impl.PrepareMessageProcesser;

public class PreprepareMessageProcesserFactory implements MessageProcesserFactory {

	public MessageProcesser createMessageProcesser() {
		return new PrepareMessageProcesser();
	}

}
