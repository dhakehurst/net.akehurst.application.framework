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
package net.akehurst.application.framework.technology.interfaceGui.elements;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventType;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphViewer;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;

public interface IGuiElement {

	String getElementId();

	Object get(UserSession session, String propertyName);

	void set(UserSession session, String propertyName, Object value);

	void addClass(UserSession session, String className);

	void removeClass(UserSession session, String className);

	void onEvent(UserSession session, GuiEventType eventType, OnEventHandler handler);

	void clear(UserSession session);

	void setDisabled(UserSession session, boolean value);

	void setLoading(UserSession session, boolean value);

	void removeSubElement(UserSession session, String subElementId);

	IGuiText createText(final UserSession session, String textId, String content);

	IGuiTable createTable(final UserSession session, String tableId, String content);

	IGuiGraphViewer createGraph(final UserSession session, String graphId);

	<X, Y> IGuiChart<X, Y> createChart(UserSession session, String chartId, String chartType, String jsonChartData, String jsonChartOptions);

	/**
	 * adds sub elements for which the content is expected to be html.
	 * <p>
	 * should not really be used as it is html specific.
	 *
	 * @param session
	 * @param newElementId
	 * @param newElementType
	 * @param attributes
	 * @param content
	 */
	void addSubElement(UserSession session, String newElementId, String newElementType, String attributes, Object content);
}
