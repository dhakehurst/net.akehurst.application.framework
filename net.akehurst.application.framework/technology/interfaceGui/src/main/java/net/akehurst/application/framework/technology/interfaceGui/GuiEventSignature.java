package net.akehurst.application.framework.technology.guiInterface;

import net.akehurst.application.framework.common.AbstractDataType;

public class GuiEventSignature extends AbstractDataType {

	public GuiEventSignature(StageIdentity stageId, SceneIdentity sceneId, String elementId, String eventType) {
		super(stageId, sceneId, elementId, eventType);
		this.stageId = stageId;
		this.sceneId = sceneId;
		this.elementId = elementId;
		this.eventType = eventType;
	}

	StageIdentity stageId;
	public StageIdentity getStageId() {
		return this.stageId;
	}
	
	SceneIdentity sceneId;
	public SceneIdentity getSceneId() {
		return this.sceneId;
	}
	
	String elementId;
	public String getElementId() {
		return this.elementId;
	}
	
	String eventType;
	public String getEventType() {
		return this.eventType;
	}
}
