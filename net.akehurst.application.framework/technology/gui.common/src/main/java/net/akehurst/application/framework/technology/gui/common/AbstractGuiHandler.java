package net.akehurst.application.framework.technology.gui.common;

import java.net.URL;

import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.realisation.AbstractActiveSignalProcessingObject;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.IGuiCallback;
import net.akehurst.application.framework.technology.guiInterface.IGuiHandler;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;

abstract
public class AbstractGuiHandler extends AbstractActiveSignalProcessingObject implements IGuiHandler, IGuiNotification {

	public AbstractGuiHandler(String id) {
		super(id);
	}

	protected IGuiRequest guiRequest;
	public IGuiRequest getGuiRequest() {
		return this.guiRequest;
	}
	public void setGuiRequest(IGuiRequest value) {
		this.guiRequest = value;
	}
	
	abstract protected void onStageCreated(GuiEvent event);
	
	abstract protected void onSceneLoaded(GuiEvent event);

	abstract protected IGuiScene getScene(SceneIdentity sceneId);
	
	// --------- IGuiNotification ---------
	
	@Override
	abstract public void notifyReady();

	@Override
	public void notifyEventOccured(GuiEvent event) {
		super.submit("notifyEventOccured", () -> {
			
			if (IGuiNotification.EVENT_STAGE_CREATED.equals(event.getSignature().getEventType())) {
				this.onStageCreated( event );
			} else if (IGuiNotification.EVENT_SCENE_LOADED.equals(event.getSignature().getEventType())) {
				this.onSceneLoaded( event );
			} else {
				this.getScene(event.getSignature().getSceneId()).notifyEventOccured(event);
			}
		});
	}

	@Override
	public void notifyDowloadRequest(UserSession session, String filename, IGuiCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyUpload(UserSession session, String filename) {
		// TODO Auto-generated method stub
		
	}

	
	
}
