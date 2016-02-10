package net.akehurst.application.framework.technology.guiInterface;

import java.util.Map;

import net.akehurst.application.framework.technology.authentication.ISession;
import net.akehurst.application.framework.technology.authentication.IUser;


public interface IGuiNotification {

	void notifyReady();
	
	void notifyStageLoaded(ISession session, String stageId);
	
	void notifySceneLoaded(ISession session, String sceneId);
	
	void notifyEventOccured(ISession session, String sceneId, String elementId, String eventType, Map<String, Object> data);
}
