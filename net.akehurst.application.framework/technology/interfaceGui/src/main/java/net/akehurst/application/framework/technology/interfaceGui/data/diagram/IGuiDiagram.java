package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IGuiDiagram {
	void add(UserSession session, IGuiDiagramData content);

	void remove(UserSession session, IGuiDiagramData content);
}
