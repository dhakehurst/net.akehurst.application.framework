package net.akehurst.application.framework.technology.gui.vertx.elements;

import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiElement;

public class VertxGuiElement implements IGuiElement {

	public VertxGuiElement(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		this.guiRequest = guiRequest;
		this.scene = scene;
		this.elementName = elementName;
	}

	IGuiRequest guiRequest;
	IGuiScene scene;
	String elementName;

	@Override
	public Object get(final UserSession session, final String propertyName) {
		// this.guiRequest.getValue();
		return null;
	}

	@Override
	public void set(final UserSession session, final String propertyName, final Object value) {
		// this.guiRequest.set
	}

	@Override
	public void onEvent(final UserSession session, final String eventType, final OnEventHandler handler) {
		this.guiRequest.requestRecieveEvent(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, eventType);
		final GuiEventSignature sig = new GuiEventSignature(this.scene.getStageId(), this.scene.getSceneId(), this.elementName, eventType);
		this.scene.onEvent(session, sig, handler);
	}

}
