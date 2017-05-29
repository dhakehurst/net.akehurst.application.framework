/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.technology.gui.vertx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.impl.UserHolder;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.sstore.LocalSessionStore;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;

public class ClientServerComms {

	public ClientServerComms(final Vertx vertx, final Router router, final AuthProvider authProvider, final String rootPath, final String busPath) {
		this.vertx = vertx;
		this.router = router;
		this.rootPath = rootPath;
		this.authProvider = authProvider;
		// for session comms
		this.socks = new HashMap<>();
		this.activeSessions = new HashMap<>();

		// for publication to all
		this.busPath = busPath;
		this.eventbus = vertx.eventBus();
		this.outbound = new ArrayList<>();
		this.inbound = new ArrayList<>();
	}

	Vertx vertx;
	Router router;
	String rootPath;
	AuthProvider authProvider;

	String busPath;
	EventBus eventbus;

	Map<String, Session> activeSessions;

	UserSession createUserSession(final Session webSession) {
		this.activeSessions.put(webSession.id(), webSession);
		UserDetails user = null;
		final UserHolder holder = webSession.get("__vertx.userHolder");
		if (null != holder && null != holder.user) {
			final String n = holder.user.principal().getString("username");
			user = new UserDetails(n);
		} else {
			// not authenticated, leave user as null
		}
		final UserSession us = new UserSession(webSession.id(), user, webSession.data());
		return us;
	}

	Session getSession(final String sessionId) {
		final Session session = this.activeSessions.get(sessionId);
		if (null == session) {
			return null;
		} else {
			if (session.isDestroyed()) {
				session.remove(sessionId);
				return null;
			} else {
				return session;
			}
		}
	}

	@FunctionalInterface
	public interface F3<T1, T2, T3> {
		void apply(T1 o1, T2 o2, T3 o3);
	}

	public void addSocksChannel(final boolean authenticated, final String authenticationRedirect, final String stagePath, final String sockjsPath,
			final F3<UserSession, String, JsonObject> handler) {
		final String socksRoutePath = stagePath + sockjsPath + "*";

		this.router.route(socksRoutePath).handler(CookieHandler.create());
		this.router.route(socksRoutePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		this.router.route(socksRoutePath)
				.handler(SessionHandler.create(LocalSessionStore.create(this.vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		this.router.route(socksRoutePath).handler(UserSessionHandler.create(this.authProvider));
		if (authenticated) {
			final String loginRedirectURL = this.rootPath.isEmpty() ? authenticationRedirect : this.rootPath + authenticationRedirect;
			this.router.route(socksRoutePath).handler(new MyAuthHandler(this.authProvider, loginRedirectURL, this.rootPath));
		}

		final SockJSHandler sockJSHandler = SockJSHandler.create(this.vertx);
		this.router.route(socksRoutePath).handler(sockJSHandler);
		sockJSHandler.socketHandler(ss -> {
			this.socks.put(ss.webSession(), ss);
			final UserSession sess = this.createUserSession(ss.webSession());
			ss.handler(b -> {
				final String s = new String(b.getBytes());
				System.out.println(s);
				final JsonObject json = new JsonObject(s);
				final String channelId = json.getString("channelId");
				final JsonObject data = json.getJsonObject("data");
				final UserSession us = sess;
				handler.apply(us, channelId, data);
			});
		});
	}

	Map<Session, SockJSSocket> socks;

	public void send(final UserSession session, final String channelId, final JsonObject data) {
		final JsonObject msg = new JsonObject();
		msg.put("channelId", channelId);
		msg.put("data", data);
		final Session sess = this.getSession(session.getId());
		final SockJSSocket ss = this.socks.get(sess);
		ss.write(Buffer.factory.buffer(msg.encode()));
	}

	List<String> outbound;
	List<String> inbound;

	void addOutboundAddress(final String address) {
		this.outbound.add(address);
		this.refreshEventBus();
	}

	void addInboundAddress(final String address) {
		this.inbound.add(address);
		this.refreshEventBus();
	}

	void refreshEventBus() {
		if (null == this.router) {

		} else {
			final BridgeOptions options = new BridgeOptions();
			for (final String a : this.outbound) {
				options.addOutboundPermitted(new PermittedOptions().setAddress(a));
			}
			for (final String a : this.inbound) {
				options.addInboundPermitted(new PermittedOptions().setAddress(a));
			}
			this.router.clear();
			this.router.route(this.busPath + "/*").handler(SockJSHandler.create(this.vertx).bridge(options));
		}
	}

	void send(final String user, final String address, final JsonObject data) {
		final DeliveryOptions options = new DeliveryOptions();
		options.addHeader("user", user);
		this.eventbus.publish(address, data, options);
	}

	void publish(final String address, final JsonObject data) {
		this.eventbus.publish(address, data);
	}
}
