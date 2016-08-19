package net.akehurst.application.framework.technology.gui.jfx.elements;

import javafx.scene.Node;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiText;

public class JfxText extends JfxGuiElement implements IGuiText {

	public JfxText(final Node n) {
		super(n);
	}

	// @Override
	// public String getText(UserSession session) {
	// return (String)super.get(session, "text");
	// }

	@Override
	public void setText(final UserSession session, final String value) {
		super.set(session, "text", value);
	}

	@Override
	public void onTextChange(final UserSession session, final EventTextChange event) {
		// TODO Auto-generated method stub

	}

}
