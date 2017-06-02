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
import net.akehurst.application.framework.technology.interfaceGui.DialogIdentity;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiDiagram;
import net.akehurst.application.framework.technology.interfaceGui.data.editor.IGuiEditor;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphViewer;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiContainer;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class VertxGuiDialogProxy implements InvocationHandler, IIdentifiableObject {

	public VertxGuiDialogProxy(final String afId, final IGuiRequest guiRequest, final IGuiScene scene, final DialogIdentity dialogId, final String content) {
		this.afId = afId;
		this.guiRequest = guiRequest;
		this.scene = scene;
		this.dialogId = dialogId;
		this.content = content;
	}

	final private String afId;
	final private IGuiRequest guiRequest;
	final private IGuiScene scene;
	final private DialogIdentity dialogId;
	final private String content;

	@ServiceReference
	IApplicationFramework af;

	@ServiceReference
	ILogger logger;

	IGuiDialog handler;

	IGuiDialog getHandler() {
		if (null == this.handler) {
			try {
				this.handler = this.af.createObject(VertxGuiDialog.class, this.afId, this.guiRequest, this.scene, this.dialogId, this.content);
			} catch (final ApplicationFrameworkException e) {
				this.logger.log(LogLevel.ERROR, e.getMessage(), e);
			}
		}
		return this.handler;
	}

	@Override
	public String afId() {
		return this.afId;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (Arrays.asList(IGuiDialog.class.getMethods()).contains(method)) {
			return method.invoke(this.getHandler(), args);
		} else {

			final Class<?> returnType = method.getReturnType();
			if (method.getName().startsWith("get")) {
				String elementName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
				elementName = this.dialogId.asPrimitive() + "_" + elementName;

				if (method.getParameterTypes().length == 1) {
					final Class<?> pt = method.getParameterTypes()[0];
					if (String.class.isAssignableFrom(pt) && args[0] instanceof String) {
						final String nameExtension = (String) args[0];
						elementName += nameExtension;
					}
				}

				if (IGuiElement.class == returnType) {
					return new VertxGuiElement(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else if (IGuiContainer.class == returnType) {
					return new VertxGuiContainer(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else if (IGuiText.class == returnType) {
					return new VertxGuiText(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else if (IGuiChart.class == returnType) {
					return new VertxGuiChart(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else if (IGuiTable.class == returnType) {
					return new VertxGuiTable(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else if (IGuiEditor.class == returnType) {
					return new VertxGuiEditor(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else if (IGuiDiagram.class == returnType) {
					return new VertxGuiDiagram(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else if (IGuiGraphViewer.class == returnType) {
					return new VertxGuiGraph(this.guiRequest, this.scene, this.getHandler(), elementName);
				} else {
					return null;
				}
			} else {
				throw new RuntimeException("Unknown method " + method);
			}
		}
	}

}
