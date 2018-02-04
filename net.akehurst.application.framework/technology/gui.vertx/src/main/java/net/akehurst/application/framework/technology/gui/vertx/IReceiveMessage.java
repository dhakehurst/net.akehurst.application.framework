package net.akehurst.application.framework.technology.gui.vertx;

import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IReceiveMessage {
    void receive(UserSession session, String channelId, Map<String, Object> data);
}
