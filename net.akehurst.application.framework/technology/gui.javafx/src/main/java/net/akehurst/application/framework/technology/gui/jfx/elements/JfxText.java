package net.akehurst.application.framework.technology.gui.jfx.elements;

import javafx.scene.Node;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.elements.IText;

public class JfxText extends JfxGuiElement implements IText {

	public JfxText(Node n) {
		super(n);
	}
	
	@Override
	public String getText(UserSession session) {
		return (String)super.get(session, "text");
	}

	@Override
	public void setText(UserSession session, String value) {
		super.set(session, "text", value);
	}

	@Override
	public void onTextChange(EventTextChange event) {
		// TODO Auto-generated method stub
		
	}

}
