package net.akehurst.application.framework.technology.interfaceGui.grid;

import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IGuiGrid {
	void create(UserSession session, String jsonOptions);

	void appendItem(UserSession session, Map<String, Object> data, int x, int y, int w, int h);

	void appendItem(UserSession session, Map<String, Object> data);

	void removeItem(UserSession session, String itemId);

	void remove(UserSession session);
}
