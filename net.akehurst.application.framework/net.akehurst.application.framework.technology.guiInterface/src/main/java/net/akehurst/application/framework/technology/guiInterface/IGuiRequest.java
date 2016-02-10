package net.akehurst.application.framework.technology.guiInterface;

import java.net.URL;

import net.akehurst.application.framework.technology.authentication.ISession;
import net.akehurst.application.framework.technology.commsInterface.ChannelIdentity;

public interface IGuiRequest {

	void createStage(String stageId, boolean authenticated, URL content);
	
	void createScene(String stageId, String sceneId, URL content);
	
	// per user
	void switchTo(ISession session, String stageId, String sceneId);
	
	void requestRecieveEvent(ISession session, String sceneId, String elementId, String eventType);
	
	void addElement(ISession session, String sceneId, String parentId, String newElementId, String type);
	
	/**
	 * 
	 * @param parentId
	 * @param newElementId
	 * @param type
	 * @param attributes as a JSON string, using single quotes, e.g. "{ 'xxx':2, 'aa':'bb' }"
	 * @param content
	 */
	void addElement(ISession session, String sceneId, String parentId, String newElementId, String type, String attributes, Object content);
	
	void setTitle(ISession session, String sceneId, String text);
	void setText(ISession session, String sceneId, String id, String text);
		
}
