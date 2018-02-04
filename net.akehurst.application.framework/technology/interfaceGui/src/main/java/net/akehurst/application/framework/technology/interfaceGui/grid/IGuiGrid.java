package net.akehurst.application.framework.technology.interfaceGui.grid;

import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public interface IGuiGrid extends IGuiElement {
    void create(UserSession session, String jsonOptions);

    void appendItem(UserSession session, Map<String, Object> data, int x, int y, int w, int h);

    void appendItem(UserSession session, Map<String, Object> data);

    void removeItem(UserSession session, String itemId);

    void remove(UserSession session);
}
