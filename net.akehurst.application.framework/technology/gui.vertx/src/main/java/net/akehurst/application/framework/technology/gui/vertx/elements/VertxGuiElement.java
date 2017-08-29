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
import net.akehurst.application.framework.technology.interfaceGui.GuiEventType;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphViewer;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

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

	@Override
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
		this.guiRequest.elementSetProperty(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, propertyName, value);
	}

	@Override
	public void addClass(final UserSession session, final String className) {
		this.guiRequest.elementAddClass(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, className);
	}

	@Override
	public void removeClass(final UserSession session, final String className) {
		this.guiRequest.elementRemoveClass(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, className);
	}

	@Override
	public void onEvent(final UserSession session, final GuiEventType eventType, final OnEventHandler handler) {
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
		this.guiRequest.elementClear(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName);
	}

	@Override
	public void setDisabled(final UserSession session, final boolean value) {
		this.guiRequest.elementSetDisabled(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, value);
	}

	@Override
	public void setLoading(final UserSession session, final boolean value) {
		this.guiRequest.elementSetLoading(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, value);
	}

	@Override
	public void addSubElement(final UserSession session, final String newElementId, final String newElementType, final String attributes,
			final Object content) {
		String childId = null;
		if (null == this.dialog) {
			childId = newElementId;
		} else {
			childId = this.dialog.getId().asPrimitive() + "_" + newElementId;
		}
		this.guiRequest.addElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, childId, newElementType, attributes, content);
	}

	@Override
	public void removeSubElement(final UserSession session, final String subElementId) {
		String childId = null;
		if (null == this.dialog) {
			childId = subElementId;
		} else {
			childId = this.dialog.getId().asPrimitive() + "_" + subElementId;
		}
		this.guiRequest.removeElement(session, this.scene.getStageId(), this.scene.getSceneId(), childId);
	}

	@Override
	public IGuiText createText(final UserSession session, final String textId, final String content) {
		String childId = null;
		if (null == this.dialog) {
			childId = textId;
		} else {
			childId = this.dialog.getId().asPrimitive() + "_" + textId;
		}
		this.guiRequest.addElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, childId, "p", "", content);
		return new VertxGuiText(this.guiRequest, this.scene, this.dialog, childId);
	}

	@Override
	public IGuiTable createTable(final UserSession session, final String tableId, final String content) {
		String childId = null;
		if (null == this.dialog) {
			childId = tableId;
		} else {
			childId = this.dialog.getId().asPrimitive() + "_" + tableId;
		}
		this.guiRequest.addElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, childId, "table", "", content);
		final VertxGuiTable tbl = new VertxGuiTable(this.guiRequest, this.scene, this.dialog, childId);
		tbl.create(session);
		return tbl;
	}

	@Override
	public IGuiGraphViewer createGraph(final UserSession session, final String graphId) {
		String childId = null;
		if (null == this.dialog) {
			childId = graphId;
		} else {
			childId = this.dialog.getId().asPrimitive() + "_" + graphId;
		}
		this.guiRequest.addElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, childId, "div", "{class:'graph'}", "");
		final VertxGuiGraph grph = new VertxGuiGraph(this.guiRequest, this.scene, this.dialog, childId);
		// grph.create(session, initialContent);
		return grph;
	}

	@Override
	public <X, Y> IGuiChart<X, Y> createChart(final UserSession session, final String chartId, final String chartType, final String jsonChartData,
			final String jsonChartOptions) {
		String childId = null;
		if (null == this.dialog) {
			childId = chartId;
		} else {
			childId = this.dialog.getId().asPrimitive() + "_" + chartId;
		}
		this.guiRequest.chartCreate(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, childId, chartType, jsonChartData,
				jsonChartOptions);
		return new VertxGuiChart<>(this.guiRequest, this.scene, this.dialog, childId);
	}
}
