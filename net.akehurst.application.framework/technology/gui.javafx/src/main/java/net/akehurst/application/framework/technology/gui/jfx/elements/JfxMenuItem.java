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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiMenuItem;
import net.akehurst.holser.reflect.BetterMethodFinder;

// can't extend JfxGuiElement because menuItems are not JfxNodes
public class JfxMenuItem implements IGuiMenuItem {

	public JfxMenuItem(final MenuItem menuItem) {
		this.menuItem = menuItem;

		final Menu parent = this.menuItem.getParentMenu();
		if (null != parent) {
			parent.addEventHandler(Menu.ON_SHOWING, (e) -> {
				if (null != this.eventVisibleWhen) {
					this.menuItem.setVisible(this.eventVisibleWhen.execute(e.getSource()));
				}
				if (null != this.eventEnabledWhen) {
					this.menuItem.setVisible(this.eventEnabledWhen.execute(e.getSource()));
				}
			});
		} else {
			final ContextMenu cm = this.menuItem.getParentPopup();
			if (null != cm) {
				cm.addEventHandler(Menu.ON_SHOWING, (e) -> {
					if (null != this.eventVisibleWhen) {
						this.menuItem.setVisible(this.eventVisibleWhen.execute(e.getSource()));
					}
				});
			}
		}

	}

	MenuItem menuItem;

	@Override
	public Object get(final UserSession session, final String propertyName) {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(this.menuItem.getClass());
			final String mName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
			final Method m = bmf.findMethod(mName);
			return m.invoke(this.menuItem);
		} catch (final Throwable t) {
			throw new RuntimeException("Unknown property " + propertyName + " on " + this.menuItem.getClass().getName(), t);
		}
	}

	@Override
	public void set(final UserSession session, final String propertyName, final Object value) {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(this.menuItem.getClass());
			final String mName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
			final Method m = bmf.findMethod(mName, value.getClass());
			m.invoke(this.menuItem);
		} catch (final Throwable t) {
			throw new RuntimeException("Unknown property " + propertyName + " on " + this.menuItem.getClass().getName(), t);
		}
	}

	@Override
	public void onEvent(final UserSession session, final String eventName, final OnEventHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSubElement(final UserSession session, final String newElementId, final String newElementType, final String attributes,
			final Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelected(final EventSelected event) {
		this.menuItem.setOnAction((e) -> {
			event.execute();
		});
	}

	EventEnabledWhen eventEnabledWhen;

	@Override
	public void enabledWhen(final EventEnabledWhen event) {
		this.eventEnabledWhen = event;
	}

	EventVisibleWhen eventVisibleWhen;

	@Override
	public void visibleWhen(final EventVisibleWhen event) {
		this.eventVisibleWhen = event;
	}
}
