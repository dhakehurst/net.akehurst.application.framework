package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IGuiGraphViewer {
	void create(UserSession session, IGuiGraphViewData initialContent);

	void update(UserSession session, IGuiGraphViewData newContent);

	void remove(UserSession session, IGuiGraphViewData content);
}
