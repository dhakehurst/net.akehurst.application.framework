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
import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractActiveSignalProcessingObject;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.IGuiCallback;
import net.akehurst.application.framework.technology.interfaceGui.IGuiHandler;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;

abstract public class AbstractGuiHandler extends AbstractActiveSignalProcessingObject implements IGuiHandler, IGuiNotification {

	public AbstractGuiHandler(final String id) {
		super(id);
		this.scenes = new HashMap<>();
	}

	protected IGuiRequest guiRequest;

	public IGuiRequest getGuiRequest() {
		return this.guiRequest;
	}

	public void setGuiRequest(final IGuiRequest value) {
		this.guiRequest = value;
	}

	abstract protected void onStageCreated(GuiEvent event);

	abstract protected void onSceneLoaded(GuiEvent event);

	Map<SceneIdentity, IGuiScene> scenes;

	public IGuiScene getScene(final SceneIdentity sceneId) {
		return this.scenes.get(sceneId);
	}

	public <T extends IGuiScene> T getScene(final SceneIdentity sceneId, final Class<T> sceneType) {
		return (T) this.getScene(sceneId);
	}

	public <T extends IGuiScene> T createScene(final StageIdentity stageId, final SceneIdentity sceneId, final Class<T> sceneClass, final URL content) {
		final T scene = this.getGuiRequest().createScene(stageId, sceneId, sceneClass, content);
		this.scenes.put(sceneId, scene);
		return scene;
	}

	// --------- IGuiNotification ---------

	@Override
	abstract public void notifyReady();

	@Override
	public void notifyEventOccured(final GuiEvent event) {
		super.submit("notifyEventOccured", () -> {

			if (IGuiNotification.EVENT_STAGE_CREATED.equals(event.getSignature().getEventType())) {
				this.onStageCreated(event);
			} else if (IGuiNotification.EVENT_SCENE_LOADED.equals(event.getSignature().getEventType())) {
				this.onSceneLoaded(event);
			} else {
				this.getScene(event.getSignature().getSceneId()).notifyEventOccured(event);
			}
		});
	}

	@Override
	public void notifyDowloadRequest(final UserSession session, final String filename, final IGuiCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyUpload(final UserSession session, final String filename) {
		// TODO Auto-generated method stub

	}

}
