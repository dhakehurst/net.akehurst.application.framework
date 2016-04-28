package net.akehurst.application.framework.technology.gui.jfx.elements;

import javafx.scene.Node;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiElement;

public class JfxGuiElement implements IGuiElement {

	public JfxGuiElement(Node jfxNode) {
		this.jfxNode = jfxNode;
	}
	Node jfxNode;
	
	
	@Override
	public Object get(UserSession session, String propertyName) {
		return null;
	}

	@Override
	public void set(UserSession session, String propertyName, Object value) {

	}

}
