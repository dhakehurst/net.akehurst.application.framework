package net.akehurst.application.framework.technology.commsInterface;

import java.util.Map;

public interface IPublishSubscribeNotification {

	void notifyPublication(ChannelIdentity channelId, Map<String, Object> data);
	
}
