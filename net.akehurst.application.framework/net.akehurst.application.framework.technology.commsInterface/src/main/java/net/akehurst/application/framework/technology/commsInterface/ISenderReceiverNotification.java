package net.akehurst.application.framework.technology.commsInterface;

import java.util.Map;

public interface ISenderReceiverNotification {

	void notifyReceiveMessage(ISenderReceiverSource source , Map<String, Object> data);
	
}
