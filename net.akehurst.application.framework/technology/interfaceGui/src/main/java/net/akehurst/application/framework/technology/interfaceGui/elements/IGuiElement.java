package net.akehurst.application.framework.technology.guiInterface.elements;

import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene.OnEventHandler;

public interface IGuiElement {

	Object get(UserSession session, String propertyName);

	void set(UserSession session, String propertyName, Object value);

	void onEvent(final UserSession session, final String eventName, OnEventHandler handler);
}
