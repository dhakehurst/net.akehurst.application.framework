package net.akehurst.application.framework.technology.gui.vertx.elements;

import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiText;

public class VertxGuiText extends VertxGuiElement implements IGuiText {

	public VertxGuiText(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		super(guiRequest, scene, elementName);
	}

	@Override
	public void setText(final UserSession session, final String value) {
		this.guiRequest.setText(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, value);
	}

	@Override
	public void onTextChange(final UserSession session, final EventTextChange event) {
		super.onEvent(session, "oninput", (e) -> event.execute());
	}

}
