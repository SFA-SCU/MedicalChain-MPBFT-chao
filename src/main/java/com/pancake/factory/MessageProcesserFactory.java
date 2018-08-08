package com.pancake.factory;

import com.pancake.message.processer.MessageProcesser;

/**
 * 
 * @author I353561
 *
 */
public interface MessageProcesserFactory {
	MessageProcesser createMessageProcesser();
}
