package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public interface IGuiDiagram extends IGuiElement {
    void create(UserSession session, IGuiDiagramData initialContent);

    void update(UserSession session, IGuiDiagramData newContent);

    void remove(UserSession session);
}
