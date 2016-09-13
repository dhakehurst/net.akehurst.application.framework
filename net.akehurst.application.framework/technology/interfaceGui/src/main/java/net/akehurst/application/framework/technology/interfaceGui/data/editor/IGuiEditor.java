package net.akehurst.application.framework.technology.interfaceGui.data.editor;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IGuiEditor {
	void add(UserSession session, String initialContent, IGuiLanguageDefinition languageDefinition);
}
