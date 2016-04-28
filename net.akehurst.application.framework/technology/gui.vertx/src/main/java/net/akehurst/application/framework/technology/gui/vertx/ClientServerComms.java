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
import java.util.function.Consumer;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Route;
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
import net.akehurst.application.framework.common.UserDetails;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;

public class ClientServerComms {

	public ClientServerComms(Vertx vertx, Router router, AuthProvider authProvider, String busPath) {
		this.vertx = vertx;
		this.router = router;
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
	AuthProvider authProvider;

	String busPath;
	EventBus eventbus;

	Map<String, Session> activeSessions;

	UserSession createTechSession(Session session) {
		this.activeSessions.put(session.id(), session);
		UserDetails user = null;
		UserHolder holder = session.get("__vertx.userHolder");
		if (null != holder && null != holder.user) {
			String n = holder.user.principal().getString("username");
			user = new UserDetails(n);
		} else {
			// not authenticated, leave user as null
		}
		return new UserSession(session.id(), user);
	}

	Session getSession(String sessionId) {
		Session session = this.activeSessions.get(sessionId);
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

	public void addSocksChannel(String socksRoutePath, F3<UserSession, String, JsonObject> handler) {
		router.route(socksRoutePath).handler(CookieHandler.create());
		router.route(socksRoutePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		router.route(socksRoutePath).handler(SessionHandler.create(LocalSessionStore.create(vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		router.route(socksRoutePath).handler(UserSessionHandler.create(authProvider));
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
		router.route(socksRoutePath).handler(sockJSHandler);
		sockJSHandler.socketHandler(ss -> {
			this.socks.put(ss.webSession(), ss);
			UserSession sess = this.createTechSession(ss.webSession());
			ss.handler(b -> {
				String s = new String(b.getBytes());
				System.out.println(s);
				JsonObject json = new JsonObject(s);
				String channelId = json.getString("channelId");
				JsonObject data = json.getJsonObject("data");
				handler.apply(sess, channelId, data);
			});
		});
	}

	Map<Session, SockJSSocket> socks;

	public void send(UserSession session, String channelId, JsonObject data) {
		JsonObject msg = new JsonObject();
		msg.put("channelId", channelId);
		msg.put("data", data);
		Session sess = this.getSession(session.getId());
		SockJSSocket ss = this.socks.get(sess);
		ss.write(Buffer.factory.buffer(msg.encode()));
	}

	List<String> outbound;
	List<String> inbound;

	void addOutboundAddress(String address) {
		this.outbound.add(address);
		this.refreshEventBus();
	}

	void addInboundAddress(String address) {
		this.inbound.add(address);
		this.refreshEventBus();
	}

	void refreshEventBus() {
		if (null == this.router) {

		} else {
			BridgeOptions options = new BridgeOptions();
			for (String a : this.outbound) {
				options.addOutboundPermitted(new PermittedOptions().setAddress(a));
			}
			for (String a : this.inbound) {
				options.addInboundPermitted(new PermittedOptions().setAddress(a));
			}
			router.clear();
			router.route(this.busPath + "/*").handler(SockJSHandler.create(vertx).bridge(options));
		}
	}

	void send(String user, String address, JsonObject data) {
		DeliveryOptions options = new DeliveryOptions();
		options.addHeader("user", user);
		this.eventbus.publish(address, data, options);
	}

	void publish(String address, JsonObject data) {
		this.eventbus.publish(address, data);
	}
}
