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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.javafx.css.Selector;

import javafx.css.Styleable;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.tree.IGuiTreeView;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiMenuItem;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

public class JfxGuiScene implements IGuiScene, InvocationHandler {

	public JfxGuiScene(final String id) {
		this.afId = id;
	}

	@Override
	public StageIdentity getStageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SceneIdentity getSceneId() {
		// TODO Auto-generated method stub
		return null;
	}

	Parent root;

	public void setRoot(final Parent value) {
		this.root = value;

		value.addEventHandler(EventType.ROOT, (e) -> {
			final UserSession session = null;
			final StageIdentity stageId = null;
			final SceneIdentity sceneId = null;
			final String elementId = null;
			final String eventType = null;
			final GuiEventSignature signature = new GuiEventSignature(stageId, sceneId, elementId, eventType);
			final Map<String, Object> eventData = new HashMap<>();
			this.notifyEventOccured(new GuiEvent(session, signature, eventData));
		});

	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (method.getName().startsWith("get")) {
			final Class<?> returnType = method.getReturnType();
			final String name = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);

			if (IGuiChart.class == returnType) {
				// Node n = this.root.lookup("#" + name);
				final Styleable s = this.lookup(this.root, "#" + name);
				if (s instanceof Chart) {
					final Chart jfx = (Chart) s;
					return new JfxChart(jfx);
				} else {
					// TODO: element not found exception
					return null;
				}
			}
			if (IGuiText.class == returnType) {
				// Node n = this.root.lookup("#" + name);
				final Styleable s = this.lookup(this.root, "#" + name);
				if (s instanceof Node) {
					return new JfxText((Node) s);
				} else {
					return null;
				}
			}
			if (IGuiMenuItem.class == returnType) {
				// this.root.lookup("#" + name);
				// can't use lookup for menuitems
				// MenuItem menuItem = this.lookupMenuItemInNodes(this.root.getChildrenUnmodifiable(), name);
				final Styleable s = this.lookup(this.root, "#" + name);
				if (s instanceof MenuItem) {
					return new JfxMenuItem((MenuItem) s);
				} else {
					return null;
				}
			}
			if (IGuiTreeView.class == returnType) {
				final Styleable s = this.lookup(this.root, "#" + name);
				if (s instanceof TreeView<?>) {
					return new JfxTreeView((TreeView<?>) s);
				} else {
					return null;
				}
			} else {
				final Node n = this.root.lookup("#" + name);
				if (n == null) {
					// not found
					return null;
				} else {
					return new JfxGuiElement(n);
				}
			}
		} else {
			return null;
		}
	}

	Styleable lookup(final Styleable self, final String selector) {
		if (null == selector) {
			return null;
		} else {
			final Selector s = Selector.createSelector(selector);
			return this.lookup(self, s);
		}
	}

	Styleable lookup(final Styleable self, final Selector selector) {
		if (null == selector) {
			return null;
		} else {
			if (selector.applies(self)) {
				return self;
			} else {
				final List<Styleable> children = new ArrayList<>();
				if (self instanceof Parent) {
					children.addAll(((Parent) self).getChildrenUnmodifiable());
				}
				if (self instanceof Control) {
					final Control c = (Control) self;
					if (null != c.getContextMenu()) {
						children.addAll(c.getContextMenu().getItems());
					}
				}
				if (self instanceof Menu) {
					final Menu m = (Menu) self;
					children.addAll(m.getItems());
				}
				for (final Styleable cs : children) {
					final Styleable r = this.lookup(cs, selector);
					if (null != r) {
						return r;
					}
				}
				return null;
			}
		}
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	@Override
	public void notifyEventOccured(final GuiEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEvent(final UserSession session, final GuiEventSignature eventSignature, final OnEventHandler handler) {
		// TODO Auto-generated method stub

	}
}
