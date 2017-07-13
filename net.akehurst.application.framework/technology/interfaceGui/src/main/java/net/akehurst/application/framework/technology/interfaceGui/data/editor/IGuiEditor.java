package net.akehurst.application.framework.technology.interfaceGui.data.editor;

import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

public interface IGuiEditor extends IGuiText {
	void create(UserSession session, String languageId, String initialContent, String optionsHJsonStr);

	void updateParseTree(final UserSession session, final String jsonParseTreeData);

	@FunctionalInterface
	interface onProvideCompletionItems {
		List<Map<String, Object>> provide(String text, int position);
	}

	void onProvideCompletionItems(UserSession session, onProvideCompletionItems handler);
}
