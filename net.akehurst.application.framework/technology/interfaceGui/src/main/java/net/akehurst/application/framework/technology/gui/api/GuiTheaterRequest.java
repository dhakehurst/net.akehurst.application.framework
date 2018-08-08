package net.akehurst.application.framework.technology.gui.api;

import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jooq.lambda.function.Consumer3;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;

public interface GuiTheaterRequest {

    void requestCreateStage(UserSession session, StageIdentity stageId, String rootPath, StageIdentity authenticationStageId,
            SceneIdentity authenticationSceneId, boolean frontEndRouting);

    void requestSend(final UserSession session, final String channelId, final JsonValue jsonData);

    void requestReceive(final String channelId, final Consumer3<UserSession, String, JsonObject> handler);

}
