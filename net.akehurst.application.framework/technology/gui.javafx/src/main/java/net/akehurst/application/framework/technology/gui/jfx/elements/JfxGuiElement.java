package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javafx.event.EventType;
import javafx.scene.Node;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;
import net.akehurst.application.framework.technology.guiInterface.StageIdentity;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiElement;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class JfxGuiElement implements IGuiElement {

	public JfxGuiElement(Node jfxNode) {
		this.jfxNode = jfxNode;
	}
	Node jfxNode;
	
	
	@Override
	public Object get(UserSession session, String propertyName) {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(this.jfxNode.getClass());
			String mName = "get"+propertyName.substring(0, 1).toUpperCase()+propertyName.substring(1);
			Method m = bmf.findMethod(mName);
			return m.invoke(this.jfxNode);
		} catch (Throwable t) {
			throw new RuntimeException("Unknown property "+propertyName+" on "+this.jfxNode.getClass().getName(), t);
		}
	}

	@Override
	public void set(UserSession session, String propertyName, Object value) {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(this.jfxNode.getClass());
			String mName = "set"+propertyName.substring(0, 1).toUpperCase()+propertyName.substring(1);
			Method m = bmf.findMethod(mName, value.getClass());
			m.invoke(this.jfxNode);
		} catch (Throwable t) {
			throw new RuntimeException("Unknown property "+propertyName+" on "+this.jfxNode.getClass().getName(), t);
		}
	}

}
