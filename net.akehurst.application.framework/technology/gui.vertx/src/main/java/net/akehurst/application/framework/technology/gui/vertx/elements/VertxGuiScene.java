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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.akehurst.application.framework.common.annotations.instance.IdentifiableObjectInstance;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.gui.common.GuiEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiContainer;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

public class VertxGuiScene implements IGuiScene, InvocationHandler {

	public VertxGuiScene(final String afId, final StageIdentity stageId, final SceneIdentity sceneId) {
		this.afId = afId;
		this.stageId = stageId;
		this.sceneId = sceneId;
	}

	IGuiRequest guiRequest;
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
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (VertxGuiScene.notifyEventOccured_method.equals(method)) {
			final GuiEvent event = (GuiEvent) args[0];
			this.notifyEventOccured(event);
		} else if (VertxGuiScene.onEvent_method.equals(method)) {
			final UserSession session = (UserSession) args[0];
			final GuiEventSignature eventSignature = (GuiEventSignature) args[1];
			final OnEventHandler handler = (OnEventHandler) args[2];
			this.onEvent(session, eventSignature, handler);
		} else {

			final Class<?> returnType = method.getReturnType();
			if (method.getName().startsWith("get") || method.getName().startsWith("set")) {
				final String elementName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);

				if (IGuiElement.class == returnType) {
					return new VertxGuiElement(this.guiRequest, this, elementName);
				} else if (IGuiContainer.class == returnType) {
					return new VertxGuiContainer(this.guiRequest, this, elementName);
				} else if (IGuiText.class == returnType) {
					return new VertxGuiText(this.guiRequest, this, elementName);
				} else if (IGuiChart.class == returnType) {
					return new VertxGuiChart(this.guiRequest, this, elementName);
				} else if (IGuiTable.class == returnType) {
					return new VertxGuiTable(this.guiRequest, this, elementName);
				} else {
					return null;
				}
			} else {
				throw new RuntimeException("Unknown method " + method);
			}
		}
		return null;
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	static Method notifyEventOccured_method;
	static {
		try {
			VertxGuiScene.notifyEventOccured_method = IGuiScene.class.getMethod("notifyEventOccured", GuiEvent.class);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifyEventOccured(final GuiEvent event) {
		if (event.getSignature().getStageId().asPrimitive().endsWith(this.stageId.asPrimitive()) && event.getSignature().getSceneId().equals(this.sceneId)) {
			this.eventHandler.handle(event);
		} else {
			// ignore it, don't handle events for other stage/scenes
		}
	}

	static Method onEvent_method;
	static {
		try {
			VertxGuiScene.onEvent_method = IGuiScene.class.getMethod("onEvent", UserSession.class, GuiEventSignature.class, OnEventHandler.class);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEvent(final UserSession session, final GuiEventSignature eventSignature, final OnEventHandler handler) {
		this.eventHandler.register(eventSignature, handler);

	}
}
