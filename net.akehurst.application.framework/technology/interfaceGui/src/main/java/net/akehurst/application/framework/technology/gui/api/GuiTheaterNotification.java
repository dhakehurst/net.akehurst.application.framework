package net.akehurst.application.framework.technology.gui.api;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;

public interface GuiTheaterNotification {

    void notifyTheaterReady(UserSession session);

    void notifyStageCreated(UserSession session, StageIdentity stageId);

}
