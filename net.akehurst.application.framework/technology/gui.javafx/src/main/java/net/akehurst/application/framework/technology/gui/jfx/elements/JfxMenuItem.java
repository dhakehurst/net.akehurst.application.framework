package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.lang.reflect.Method;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiMenuItem;
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
