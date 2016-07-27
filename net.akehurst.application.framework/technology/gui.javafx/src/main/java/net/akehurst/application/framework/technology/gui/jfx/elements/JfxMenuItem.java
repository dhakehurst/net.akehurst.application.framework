package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.lang.reflect.Method;

import javafx.scene.control.MenuItem;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiMenuItem;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class JfxMenuItem implements IGuiMenuItem {

	public JfxMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}
	MenuItem menuItem;
	
	@Override
	public Object get(UserSession session, String propertyName) {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(this.menuItem.getClass());
			String mName = "get"+propertyName.substring(0, 1).toUpperCase()+propertyName.substring(1);
			Method m = bmf.findMethod(mName);
			return m.invoke(this.menuItem);
		} catch (Throwable t) {
			throw new RuntimeException("Unknown property "+propertyName+" on "+this.menuItem.getClass().getName(), t);
		}
	}

	@Override
	public void set(UserSession session, String propertyName, Object value) {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(this.menuItem.getClass());
			String mName = "set"+propertyName.substring(0, 1).toUpperCase()+propertyName.substring(1);
			Method m = bmf.findMethod(mName, value.getClass());
			m.invoke(this.menuItem);
		} catch (Throwable t) {
			throw new RuntimeException("Unknown property "+propertyName+" on "+this.menuItem.getClass().getName(), t);
		}
	}

	@Override
	public void onSelected(EventSelected event) {
		this.menuItem.setOnAction((e)->{
			event.execute();
		});
	}
	
	
	
}
