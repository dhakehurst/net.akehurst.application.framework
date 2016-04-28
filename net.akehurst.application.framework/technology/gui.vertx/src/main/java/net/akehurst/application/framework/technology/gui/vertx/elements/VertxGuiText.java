package net.akehurst.application.framework.technology.gui.vertx.elements;

import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;
import net.akehurst.application.framework.technology.guiInterface.StageIdentity;
import net.akehurst.application.framework.technology.guiInterface.elements.IText;

public class VertxGuiText extends VertxGuiElement implements IText {

	public VertxGuiText(IGuiRequest guiRequest, StageIdentity stageId, SceneIdentity sceneId, String elementName) {
		super(guiRequest, stageId, sceneId, elementName);
	}

	@Override
	public String getText(UserSession session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setText(UserSession session, String value) {
		this.guiRequest.setText(session, this.stageId, this.sceneId, this.elementName, value);
	}

	@Override
	public void onTextChange(EventTextChange event) {
		// TODO Auto-generated method stub
		
	}

	
	
}
