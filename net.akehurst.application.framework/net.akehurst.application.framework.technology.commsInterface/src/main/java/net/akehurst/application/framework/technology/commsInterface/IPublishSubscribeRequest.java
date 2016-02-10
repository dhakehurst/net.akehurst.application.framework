package net.akehurst.application.framework.technology.commsInterface;

import java.util.List;
import java.util.Map;

public interface IPublishSubscribeRequest {

	void requestSubscribeTo(ChannelIdentity channelId);
	void requestPublisherOf(ChannelIdentity channelId);
	
	<T> void requestPublish(ChannelIdentity channelId, Map<String, Object> data);
	
}
