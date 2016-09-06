package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.io.InputStream;
import java.util.Scanner;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;

public class VertxGuiDialog extends VertxGuiScene implements IGuiDialog {

	public VertxGuiDialog(final String afId, final IGuiRequest guiRequest, final IGuiScene scene, final String dialogId, final String dialogContent) {
		super(afId, guiRequest, scene.getStageId(), scene.getSceneId());
		this.scene = scene;
		this.dialogId = dialogId;
		this.dialogContent = dialogContent;
	}

	IGuiScene scene;
	String dialogId;
	String dialogContent;

	@Override
	public void show(final UserSession session) {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream(this.dialogContent + ".html");
		final Scanner s = new Scanner(is);
		String content = "";
		while (s.hasNextLine()) {
			content += s.nextLine();
		}
		s.close();
		this.guiRequest.showDialog(session, this.stageId, this.sceneId, this.dialogId, content);
	}

	@Override
	public void close(final UserSession session) {
		this.guiRequest.removeElement(session, this.stageId, this.sceneId, this.dialogId);
	}

	// TODO: create own set of event handlers, must unregister them when dialog closes
}
