package net.akehurst.application.framework.technology.gui.vertx.elements;

import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;
import net.akehurst.application.framework.technology.guiInterface.StageIdentity;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiElement;

public class VertxGuiElement implements IGuiElement {

	public VertxGuiElement(IGuiRequest guiRequest, StageIdentity stageId, SceneIdentity sceneId, String elementName) {
		this.guiRequest = guiRequest;
		this.stageId = stageId;
		this.sceneId = sceneId;
		this.elementName = elementName;
	}
	
	IGuiRequest guiRequest;
	StageIdentity stageId;
	SceneIdentity sceneId;
	String elementName;
	
	
	@Override
	public Object get(UserSession session, String propertyName) {
		//this.guiRequest.getValue();
		return null;
	}

	@Override
	public void set(UserSession session, String propertyName, Object value) {
		//this.guiRequest.set
	}

}
