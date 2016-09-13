package net.akehurst.application.framework.technology.interfaceGui.data.editor;

import java.util.List;

public interface IGuiLanguageDefinition {

	String getIdentity();

	List<IGuiSyntaxHighlightDefinition> getSyntaxHighlighting();
}
