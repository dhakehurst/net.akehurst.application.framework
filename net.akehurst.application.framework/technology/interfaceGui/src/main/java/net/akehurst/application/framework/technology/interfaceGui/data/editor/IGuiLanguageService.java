package net.akehurst.application.framework.technology.interfaceGui.data.editor;

import java.util.List;

public interface IGuiLanguageService {

	String getIdentity();

	List<IGuiSyntaxHighlightDefinition> getSyntaxHighlighting();

	void load();

	void revert();

	void save();

	void update();

	void assist();

	void validate();

	void hover();

	void highlight();

	void occurrences();

	void format();
}
