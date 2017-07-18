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
package net.akehurst.application.framework.technology.gui.common;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractActiveSignalProcessingObject;
import net.akehurst.application.framework.technology.interfaceGui.DialogIdentity;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiException;
import net.akehurst.application.framework.technology.interfaceGui.IGuiCallback;
import net.akehurst.application.framework.technology.interfaceGui.IGuiHandler;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.IGuiSceneHandler;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractGuiHandler extends AbstractActiveSignalProcessingObject implements IGuiHandler, IGuiNotification {

	public AbstractGuiHandler(final String id) {
		super(id);
		this.scenes = new HashMap<>();
		this.sceneHandlers = new HashMap<>();
	}

	public IGuiRequest guiRequest;

	public IGuiRequest getGuiRequest() {
		return this.guiRequest;
	}

	public void setGuiRequest(final IGuiRequest value) {
		this.guiRequest = value;
	}

	abstract protected void onStageCreated(GuiEvent event);

	protected void onStageClosed(final GuiEvent event) {
		this.afTerminate();
	}

	protected void onSceneLoaded(final GuiEvent event) {
		try {
			final SceneIdentity currentSceneId = event.getSignature().getSceneId();
			final IGuiScene scene = this.getScene(currentSceneId);
			final IGuiSceneHandler handler = this.sceneHandlers.get(event.getSignature().getSceneId());
			if (null == handler) {
				// scene not found...do nothing !
				this.logger.log(LogLevel.DEBUG, "Scene %s not found for %s", currentSceneId, this.afId());
			} else {
				handler.loaded(this, scene, event);
			}
		} catch (final Throwable e) {
			this.logger.log(LogLevel.ERROR, "Error loading scene", e);
		}
	}

	Map<SceneIdentity, IGuiScene> scenes;
	Map<SceneIdentity, IGuiSceneHandler> sceneHandlers;

	@Override
	public void addAuthentication(final UserSession session) throws GuiException {
		this.getGuiRequest().addAuthentication(session);
	}

	@Override
	public void clearAuthentication(final UserSession session) throws GuiException {
		this.getGuiRequest().clearAuthentication(session);
	}

	@Override
	public void navigateTo(final UserSession session, final String location) {
		this.guiRequest.navigateTo(session, location);
	}

	@Override
	public IGuiScene getScene(final SceneIdentity sceneId) {
		return this.scenes.get(sceneId);
	}

	@Override
	public <T extends IGuiScene> T getScene(final SceneIdentity sceneId, final Class<T> sceneType) {
		return (T) this.getScene(sceneId);
	}

	@Override
	public <T extends IGuiScene> T createScene(final StageIdentity stageId, final SceneIdentity sceneId, final Class<T> sceneClass,
			final IGuiSceneHandler sceneHandler, final URL content) {
		final T scene = this.guiRequest.createScene(stageId, sceneId, sceneClass, content);
		this.scenes.put(sceneId, scene);
		this.sceneHandlers.put(sceneId, sceneHandler);
		return scene;
	}

	// --------- IGuiNotification ---------

	@Override
	abstract public void notifyReady();

	@Override
	public void notifyEventOccured(final GuiEvent event) {
		super.submit("notifyEventOccured", () -> {

			switch (event.getSignature().getEventType()) {

				case SCENE_LOADED:
					this.onSceneLoaded(event);
				break;

				case STAGE_CLOSED:
					this.onStageClosed(event);
				break;

				case STAGE_CREATED:
					this.onStageCreated(event);
				break;

				case CLICK:
				case CHANGE:
				default: {
					final SceneIdentity currentSceneId = event.getSignature().getSceneId();
					final IGuiScene scene = this.getScene(currentSceneId);
					if (null == scene) {
						// scene not found...do nothing !
						this.logger.log(LogLevel.DEBUG, "Scene %s not found for %s", currentSceneId, this.afId());
					} else {
						final DialogIdentity dialogId = event.getSignature().getDialogId();
						if (null == dialogId) {
							scene.notifyEventOccured(event);
						} else {
							scene.getDialog(dialogId).notifyEventOccured(event);
						}
					}
				}
				break;

			}

		});
	}

	@Override
	public void notifyDowloadRequest(final UserSession session, final Map<String, List<String>> params, final IGuiCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyUpload(final UserSession session, final String filename) {
		// TODO Auto-generated method stub

	}

}
