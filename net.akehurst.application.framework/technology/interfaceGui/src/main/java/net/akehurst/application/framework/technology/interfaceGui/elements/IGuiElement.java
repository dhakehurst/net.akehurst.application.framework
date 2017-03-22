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
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;

public interface IGuiElement {

	Object get(UserSession session, String propertyName);

	void set(UserSession session, String propertyName, Object value);

	void onEvent(UserSession session, String eventName, OnEventHandler handler);

	void clear(UserSession session);

	void addSubElement(UserSession session, String newElementId, String newElementType, String attributes, Object content);

	void createChart(final UserSession session, String chartId, String chartType, String jsonChartData, String jsonChartOptions);
}
