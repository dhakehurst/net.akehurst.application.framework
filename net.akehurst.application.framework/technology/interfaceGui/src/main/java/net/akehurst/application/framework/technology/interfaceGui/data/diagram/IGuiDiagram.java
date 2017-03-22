package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public interface IGuiDiagram extends IGuiElement {
	void add(UserSession session, IGuiDiagramData content);

	void remove(UserSession session, IGuiDiagramData content);
}
