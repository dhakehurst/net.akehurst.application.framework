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
package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.lang.reflect.Method;

import javafx.scene.Node;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventType;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class JfxGuiElement implements IGuiElement {

	public JfxGuiElement(final Node jfxNode) {
		this.jfxNode = jfxNode;
	}

	Node jfxNode;

	@Override
	public Object get(final UserSession session, final String propertyName) {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(this.jfxNode.getClass());
			final String mName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
			final Method m = bmf.findMethod(mName);
			return m.invoke(this.jfxNode);
		} catch (final Throwable t) {
			throw new RuntimeException("Unknown property " + propertyName + " on " + this.jfxNode.getClass().getName(), t);
		}
	}

	@Override
	public void set(final UserSession session, final String propertyName, final Object value) {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(this.jfxNode.getClass());
			final String mName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
			final Method m = bmf.findMethod(mName, value.getClass());
			m.invoke(this.jfxNode, value);
		} catch (final Throwable t) {
			throw new RuntimeException("Unknown property " + propertyName + " on " + this.jfxNode.getClass().getName(), t);
		}
	}

	@Override
	public void onEvent(final UserSession session, final GuiEventType eventType, final OnEventHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear(final UserSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disable(final UserSession session, final boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSubElement(final UserSession session, final String newElementId, final String newElementType, final String attributes,
			final Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createChart(final UserSession session, final String chartId, final String chartType, final String jsonChartData,
			final String jsonChartOptions) {
		// TODO Auto-generated method stub

	}
}
