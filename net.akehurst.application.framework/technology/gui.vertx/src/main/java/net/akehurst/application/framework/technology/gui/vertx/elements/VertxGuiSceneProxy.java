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
import java.util.Arrays;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiDiagram;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiContainer;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class VertxGuiSceneProxy implements InvocationHandler, IIdentifiableObject {

	public VertxGuiSceneProxy(final String afId, final IGuiRequest guiRequest, final StageIdentity stageId, final SceneIdentity sceneId) {
		this.handler = null;
		this.guiRequest = guiRequest;
		this.afId = afId;
		this.stageId = stageId;
		this.sceneId = sceneId;
	}

	String afId;
	StageIdentity stageId;
	SceneIdentity sceneId;

	@ServiceReference
	IApplicationFramework af;

	@ServiceReference
	ILogger logger;

	IGuiScene handler;

	IGuiScene getHandler() {
		if (null == this.handler) {
			try {
				this.handler = this.af.createObject(VertxGuiScene.class, this.afId, this.guiRequest, this.stageId, this.sceneId);
			} catch (final ApplicationFrameworkException e) {
				this.logger.log(LogLevel.ERROR, e.getMessage(), e);
			}
		}
		return this.handler;
	}

	IGuiRequest guiRequest;

	@Override
	public String afId() {
		return this.afId;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (Arrays.asList(IGuiScene.class.getMethods()).contains(method)) {
			return method.invoke(this.getHandler(), args);
		} else {

			final Class<?> returnType = method.getReturnType();
			if (method.getName().startsWith("get")) {
				String elementName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);

				if (method.getParameterTypes().length == 1) {
					final Class<?> pt = method.getParameterTypes()[0];
					if (String.class.isAssignableFrom(pt) && args[0] instanceof String) {
						final String nameExtension = (String) args[0];
						elementName += nameExtension;
					}
				}

				if (IGuiElement.class == returnType) {
					return new VertxGuiElement(this.guiRequest, this.getHandler(), elementName);
				} else if (IGuiContainer.class == returnType) {
					return new VertxGuiContainer(this.guiRequest, this.getHandler(), elementName);
				} else if (IGuiText.class == returnType) {
					return new VertxGuiText(this.guiRequest, this.getHandler(), elementName);
				} else if (IGuiChart.class == returnType) {
					return new VertxGuiChart(this.guiRequest, this.getHandler(), elementName);
				} else if (IGuiTable.class == returnType) {
					return new VertxGuiTable(this.guiRequest, this.getHandler(), elementName);
				} else if (IGuiDiagram.class == returnType) {
					return new VertxGuiDiagram(this.guiRequest, this.getHandler(), elementName);
				} else {
					return null;
				}
			} else {
				throw new RuntimeException("Unknown method " + method);
			}
		}
	}

}
