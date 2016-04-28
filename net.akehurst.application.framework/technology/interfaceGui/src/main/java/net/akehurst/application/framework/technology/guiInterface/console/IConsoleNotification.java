package net.akehurst.application.framework.technology.guiInterface.console;

import net.akehurst.application.framework.common.UserSession;

public interface IConsoleNotification {

	void notifyReady(UserSession session);
	
	void notifyKeyPress(UserSession session);
}
