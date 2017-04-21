package net.akehurst.application.framework.technology.interfaceGui.data.editor;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public interface IGuiEditor extends IGuiElement {
	void create(UserSession session, String languageId, String initialContent);

	void updateParseTree(final UserSession session, final String jsonParseTreeData);
}
