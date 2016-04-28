package net.akehurst.application.framework.technology.guiInterface.elements;

import net.akehurst.application.framework.common.UserSession;

public interface IGuiElement {

	Object get(UserSession session, String propertyName);
	
	void set(UserSession session, String propertyName, Object value);
	
}
