package com.pancake.factory.impl;

import com.pancake.factory.MessageProcesserFactory;
import com.pancake.message.processer.MessageProcesser;
import com.pancake.message.processer.impl.CommitMessageProcesser;

public class CommitMessageProcesserFactory implements MessageProcesserFactory {

	public MessageProcesser createMessageProcesser() {
		return new CommitMessageProcesser();
	}

}
