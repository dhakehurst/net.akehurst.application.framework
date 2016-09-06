package net.akehurst.application.framework.technology.interfaceGui;

import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;

public interface IGuiDialog extends IIdentifiableObject {
	/**
	 * Register an event handler for an event.
	 *
	 * @param session
	 * @param eventSignature
	 * @param handler
	 */
	void onEvent(UserSession session, GuiEventSignature eventSignature, OnEventHandler handler);

	void show(UserSession session);

	void close(UserSession session);
}
