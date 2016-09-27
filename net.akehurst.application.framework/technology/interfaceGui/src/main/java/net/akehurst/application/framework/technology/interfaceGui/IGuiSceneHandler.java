package net.akehurst.application.framework.technology.interfaceGui;

import java.net.URL;

public interface IGuiSceneHandler {
	IGuiScene createScene(final IGuiHandler gui, final StageIdentity stageId, final URL content);

	void loaded(final IGuiHandler gui, final IGuiScene guiScene, final GuiEvent event);
}
