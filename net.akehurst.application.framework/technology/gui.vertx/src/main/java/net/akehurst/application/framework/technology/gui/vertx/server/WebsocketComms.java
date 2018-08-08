package net.akehurst.application.framework.technology.gui.vertx.server;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jooq.lambda.function.Consumer2;
import org.jooq.lambda.function.Consumer3;

import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Cookie;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.IdentifiableObjectAbstract;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class WebsocketComms extends IdentifiableObjectAbstract implements Handler<ServerWebSocket> {

    @ServiceReference
    public ILogger logger;

    private final Map<String, ServerWebSocket> sockets;
    private final Map<String, Consumer2<String, Buffer>> socketHandlers;
    private final Map<String, Consumer3<UserSession, String, JsonObject>> messageHandlers;

    public WebsocketComms(final String afId) {
        super(afId);
        this.sockets = new HashMap<>();
        this.socketHandlers = new HashMap<>();
        this.messageHandlers = new HashMap<>();
    }

    private void receiveMessage(final String sessionId, final String message) {
        this.logger.log(LogLevel.TRACE, "receiveMessage " + message);
        try {
            final UserDetails user = null;
            final UserSession session = new UserSession(sessionId, user, null);
            final JsonObject json = JsonValue.readJSON(message).asObject();
            final String channelId = json.getString("channelId", null);
            final JsonObject data = json.get("data").asObject();
            final Consumer3<UserSession, String, JsonObject> mh = this.messageHandlers.get(channelId);
            if (null != mh) {
                mh.accept(session, channelId, data);
            } else {
                this.logger.log(LogLevel.WARN, "No handler for message " + message);
            }
        } catch (final Exception e) {
            this.logger.log(LogLevel.ERROR, "Receiving message " + message);
            this.logger.log(LogLevel.ERROR, "Exception", e);
        }
    }

    private Map<String, Cookie> createCookie(final String cookieHeader) {
        if (cookieHeader != null) {
            final Map<String, Cookie> cookies = new HashMap<>();
            final Set<io.netty.handler.codec.http.cookie.Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
            for (final io.netty.handler.codec.http.cookie.Cookie nettyCookie : nettyCookies) {
                final Cookie ourCookie = Cookie.cookie(nettyCookie);
                cookies.put(ourCookie.getName(), ourCookie);
            }
            return cookies;
        } else {
            return null;
        }
    }

    @Override
    public void handle(final ServerWebSocket socket) {
        final String ckStr = socket.headers().get("Cookie");
        final Cookie ck = this.createCookie(ckStr).get("vertx-web.session");
        final String sessionId = ck.getValue();
        final String socketPath = socket.path();
        final Consumer2<String, Buffer> h = this.socketHandlers.get(socketPath);

        if (null == h) {
            this.logger.log(LogLevel.ERROR, "Websocket path %s not registered", socketPath);
            socket.reject();
        } else {
            this.logger.log(LogLevel.INFO, "Connected to websocket, session " + sessionId + " from " + socket.remoteAddress().host());
            socket.handler(b -> {
                this.logger.log(LogLevel.TRACE, "recieved " + b + " from " + sessionId + " at " + socketPath);
                h.accept(sessionId, b);
            });
            this.sockets.put(sessionId, socket);
        }
    }

    public void connect(final String socketPath) {
        this.logger.log(LogLevel.INFO, "WebSocket path created at " + socketPath);
        this.socketHandlers.put(socketPath, (sessId, b) -> {
            final String content = new String(b.getBytes(), StandardCharsets.UTF_8);
            this.receiveMessage(sessId, content);
        });
    }

    public void send(final UserSession session, final String channelId, final JsonValue jsonData) {
        final JsonObject packet = new JsonObject();
        packet.add("channelId", channelId);
        packet.add("data", jsonData);
        final ServerWebSocket ss = this.sockets.get(session.getId());
        ss.writeTextMessage(packet.toString());
        this.logger.log(LogLevel.TRACE, "publish to " + session.getId() + " on channel " + channelId + " data " + jsonData);
    }

    public void receive(final String channelId, final Consumer3<UserSession, String, JsonObject> handler) {
        this.messageHandlers.put(channelId, handler);
    }

}
