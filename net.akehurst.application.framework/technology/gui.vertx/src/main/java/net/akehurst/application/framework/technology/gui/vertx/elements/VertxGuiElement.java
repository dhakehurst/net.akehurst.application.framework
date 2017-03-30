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

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.DialogIdentity;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public class VertxGuiElement implements IGuiElement {

	public VertxGuiElement(final IGuiRequest guiRequest, final IGuiScene scene, final IGuiDialog dialog, final String elementName) {
		this.guiRequest = guiRequest;
		this.scene = scene;
		this.dialog = dialog;
		this.elementName = elementName;
	}

	private final IGuiRequest guiRequest;
	private final IGuiScene scene;
	protected final IGuiDialog dialog;
	private final String elementName;

	protected IGuiRequest getGuiRequest() {
		return this.guiRequest;
	}

	protected IGuiScene getScene() {
		return this.scene;
	}

	protected IGuiDialog getDialog() {
		return this.dialog;
	}

	public String getElementId() {
		return this.elementName;
	}

	@Override
	public Object get(final UserSession session, final String propertyName) {
		// this.guiRequest.getValue();
		return null;
	}

	@Override
	public void set(final UserSession session, final String propertyName, final Object value) {
		this.guiRequest.set(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, propertyName, value);
	}

	@Override
	public void onEvent(final UserSession session, final String eventType, final OnEventHandler handler) {
		this.guiRequest.requestRecieveEvent(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, eventType);
		final DialogIdentity dialogId = null == this.dialog ? null : this.dialog.getId();
		final GuiEventSignature eventSignature = new GuiEventSignature(this.scene.getStageId(), this.scene.getSceneId(), dialogId, this.elementName, eventType);
		if (null == this.dialog) {
			this.scene.onEvent(session, eventSignature, handler);
		} else {
			this.dialog.onEvent(session, eventSignature, handler);
		}
	}

	@Override
	public void clear(final UserSession session) {
		this.guiRequest.clearElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName);
	}

	@Override
	public void addSubElement(final UserSession session, final String newElementId, final String newElementType, final String attributes,
			final Object content) {
		this.guiRequest.addElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, newElementId, newElementType, attributes,
				content);
	}

	@Override
	public void createChart(final UserSession session, final String chartId, final String chartType, final String jsonChartData,
			final String jsonChartOptions) {
		this.guiRequest.chartCreate(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, chartId, chartType, jsonChartData,
				jsonChartOptions);
	}
}
