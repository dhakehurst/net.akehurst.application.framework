package net.akehurst.application.framework.technology.guiInterface;

import net.akehurst.application.framework.common.AbstractDataType;

public class GuiEventSignature extends AbstractDataType {

	public GuiEventSignature(String stageId, String sceneId, String elementId, String eventType) {
		super(stageId, sceneId, elementId, eventType);
		this.stageId = stageId;
		this.sceneId = sceneId;
		this.elementId = elementId;
		this.eventType = eventType;
	}

	String stageId;
	public String getStageId() {
		return this.stageId;
	}
	
	String sceneId;
	public String getSceneId() {
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
