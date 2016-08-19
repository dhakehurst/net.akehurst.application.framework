package net.akehurst.application.framework.technology.guiInterface.elements;

import net.akehurst.application.framework.common.UserSession;

public interface IGuiText {

	void setText(UserSession session, String value);

	interface EventTextChange {
		void execute();
	}

	void onTextChange(UserSession session, EventTextChange event);

}
