package net.akehurst.application.framework.technology.commsInterface;

import java.util.Map;

public interface ISenderReceiverRequest {

	void requestSenderOf();
	void requestReceiverFor();
	
	void requestSendMessage(ISenderReceiverDestination destination, Map<String, Object> data);
}
