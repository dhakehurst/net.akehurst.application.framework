package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.annotations.instance.IdentifiableObjectInstance;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.gui.common.GuiEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.DialogIdentity;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public class VertxGuiScene implements IGuiScene {
	public VertxGuiScene(final String afId, final IGuiRequest guiRequest, final StageIdentity stageId, final SceneIdentity sceneId) {
		this.afId = afId;
		this.stageId = stageId;
		this.sceneId = sceneId;
		this.guiRequest = guiRequest;
		this.dialogs = new HashMap<>();
	}

	Map<DialogIdentity, IGuiDialog> dialogs;

	IGuiRequest guiRequest;

	protected IGuiRequest getGuiRequest() {
		return this.guiRequest;
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	StageIdentity stageId;

	@Override
	public StageIdentity getStageId() {
		return this.stageId;
	}

	SceneIdentity sceneId;

	@Override
	public SceneIdentity getSceneId() {
		return this.sceneId;
	}

	@IdentifiableObjectInstance
	GuiEventHandler eventHandler;

	public void setGuiRequest(final IGuiRequest value) {
		this.guiRequest = value;
	}

	@Override
	public void switchTo(final UserSession session) {
		this.guiRequest.switchTo(session, this.stageId, this.sceneId, new HashMap<>());
	}

	@Override
	public void switchTo(final UserSession session, final Map<String, String> sceneArguments) {
		this.guiRequest.switchTo(session, this.stageId, this.sceneId, sceneArguments);
	}

	@Override
	public void notifyEventOccured(final GuiEvent event) {
		if (event.getSignature().getStageId().asPrimitive().endsWith(this.stageId.asPrimitive()) && event.getSignature().getSceneId().equals(this.sceneId)) {
			this.eventHandler.handle(event);
		} else {
			// ignore it, don't handle events for other stage/scenes
		}
	}

	@Override
	public void onEvent(final UserSession session, final GuiEventSignature eventSignature, final OnEventHandler handler) {
		this.eventHandler.register(eventSignature, handler);

	}

	@Override
	public IGuiElement getElement(final String elementName) {
		return new VertxGuiElement(this.guiRequest, this, null, elementName);
	}

	@Override
	public <T extends IGuiDialog> T createDialog(final Class<T> dialogClass, final UserSession session, final DialogIdentity dialogId, final String title,
			final String dialogContent) {
		final T dialog = this.guiRequest.createDialog(dialogClass, session, this, dialogId, title, dialogContent);
		this.dialogs.put(dialogId, dialog);
		return dialog;
	}

	@Override
	public IGuiDialog getDialog(final DialogIdentity dialogId) {
		return this.dialogs.get(dialogId);
	}
}
