package net.akehurst.application.framework.technology.interfaceGui;

import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IGuiRequestMessage {
    Object receive(UserSession session, String channelId, Map<String, Object> data);
}
