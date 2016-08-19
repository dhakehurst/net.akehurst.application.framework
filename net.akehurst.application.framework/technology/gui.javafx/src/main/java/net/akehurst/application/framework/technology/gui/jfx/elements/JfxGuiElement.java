package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.lang.reflect.Method;

import javafx.scene.Node;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiElement;
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
	public void onEvent(final UserSession session, final String eventName, final OnEventHandler handler) {
		// TODO Auto-generated method stub

	}

}
