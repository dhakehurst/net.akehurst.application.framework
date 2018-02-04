package net.akehurst.application.framework.technology.interfaceGui.data.editor;

import java.util.List;
import java.util.Map;

import org.jooq.lambda.tuple.Tuple3;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

public interface IGuiEditor extends IGuiText {
    void create(UserSession session, String languageId, String initialContent, String optionsHJsonStr);

    void updateParseTree(final UserSession session, final String jsonParseTreeData);

    /**
     * define mapping from grammar rule names (which become to parse tree node names) to triple (foreground-colour, fontStyle, background-colour)
     *
     * @param theme
     *            (ruleName, (foreground-colour, fontStyle, background-colour))
     */
    void defineTextColourTheme(UserSession session, String themeName, final Map<String, Tuple3<String, String, String>> theme);

    @FunctionalInterface
    interface onProvideCompletionItems {
        List<Map<String, Object>> provide(String text, int position);
    }

    void onProvideCompletionItems(UserSession session, onProvideCompletionItems handler);
}
