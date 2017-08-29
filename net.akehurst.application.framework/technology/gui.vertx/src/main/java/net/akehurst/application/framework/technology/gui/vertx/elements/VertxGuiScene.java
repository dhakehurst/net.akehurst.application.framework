/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	@Override
	public IGuiRequest getGuiRequest() {
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

	// @Override
	// public void navigateTo(final UserSession session, final String location) {
	// this.guiRequest.navigateTo(session, location);
	// }
	//
	// @Override
	// public void newWindow(final UserSession session, final String location) {
	// this.guiRequest.newWindow(session, location);
	// }

	@Override
	public void switchTo(final UserSession session) {
		this.guiRequest.switchTo(session, this.stageId, this.sceneId, new HashMap<>());
	}

	@Override
	public void switchTo(final UserSession session, final Map<String, String> sceneArguments) {
		this.guiRequest.switchTo(session, this.stageId, this.sceneId, sceneArguments);
	}

	// @Override
	// public Future<String> oauthAuthorise(final UserSession session, final String clientId, final String clientSecret, final String site, final String
	// tokenPath,
	// final String authorisationPath, final String scopes) {
	// return this.guiRequest.oauthAuthorise(session, clientId, clientSecret, site, tokenPath, authorisationPath, scopes);
	// }

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
	public void onUpload(final UserSession session, final String uploadLink, final String filenameElementId, final OnEventHandler handler) {
		this.guiRequest.upload(session, this.stageId, this.sceneId, uploadLink, filenameElementId, handler);

	}

	@Override
	public IGuiElement getElement(final String elementName) {
		return new VertxGuiElement(this.guiRequest, this, null, elementName);
	}

	@Override
	public <T extends IGuiDialog> T createDialog(final Class<T> dialogClass, final UserSession session, final DialogIdentity dialogId, final String title,
			final String dialogContent) {
		final T dialog = this.guiRequest.dialogCreate(dialogClass, session, this, dialogId, title, dialogContent);
		this.dialogs.put(dialogId, dialog);
		return dialog;
	}

	@Override
	public IGuiDialog getDialog(final DialogIdentity dialogId) {
		return this.dialogs.get(dialogId);
	}
}
