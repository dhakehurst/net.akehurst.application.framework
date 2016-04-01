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

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.impl.UserHolder;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.sstore.LocalSessionStore;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.authentication.IUser;
import net.akehurst.application.framework.technology.authentication.TechSession;
import net.akehurst.application.framework.technology.authentication.TechUserDetails;
import net.akehurst.application.framework.technology.guiInterface.IGuiCallback;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;

public class AVerticle implements Verticle {

	public AVerticle(VertxWebsite ws, int port) {
		this.port = port;
		this.ws = ws;
	}

	int port;
	VertxWebsite ws;

	void addRoute(String stagePath, Handler<RoutingContext> requestHandler, String webroot, Map<String,String> variables) {
		String routePath =  stagePath + "/*";

		String sockjsCommsPath = stagePath+ws.getSockjsPath()+"/*";
		this.comms.addSocksChannel(sockjsCommsPath, (session, channelId, data) -> {
			if ("IGuiNotification.notifyEventOccured".equals(channelId)) {
				String stageId = data.getString("stageId");
				String sceneId = data.getString("sceneId");
				String eventType = data.getString("eventType");
				String elementId = data.getString("elementId");
				Map<String, Object> eventData = (Map<String, Object>) data.getJsonObject("eventData").getMap();
				this.ws.portGui().out(IGuiNotification.class).notifyEventOccured(session, stageId, sceneId, elementId, eventType, eventData);
			} else {
				// ??
			}
		});
		
		router.route(routePath).handler(CookieHandler.create());
		router.route(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		router.route(routePath).handler(SessionHandler.create(LocalSessionStore.create(vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		router.route(routePath).handler(UserSessionHandler.create(authProvider));

		router.route(routePath).handler(requestHandler);
		router.route(routePath).handler(TemplateStaticHandler.create().addVariables(variables).setCachingEnabled(false).setWebRoot(webroot));

	}

	void addAuthenticatedRoute(String stagePath, Handler<RoutingContext> requestHandler, String webroot, Map<String,String> variables) {
		String routePath =  stagePath + "/*";
		
		String sockjsCommsPath = stagePath+ws.getSockjsPath()+"/*";
		this.comms.addSocksChannel(sockjsCommsPath, (session, channelId, data) -> {
			if ("IGuiNotification.notifyEventOccured".equals(channelId)) {
				String stageId = data.getString("stageId");
				String sceneId = data.getString("sceneId");
				String eventType = data.getString("eventType");
				String elementId = data.getString("elementId");
				Map<String, Object> eventData = (Map<String, Object>) data.getJsonObject("eventData").getMap();
				this.ws.portGui().out(IGuiNotification.class).notifyEventOccured(session, stageId, sceneId, elementId, eventType, eventData);
			} else {
				// ??
			}
		});
		
		router.route(routePath).handler(CookieHandler.create());
		router.route(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		router.route(routePath).handler(SessionHandler.create(LocalSessionStore.create(vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		router.route(routePath).handler(UserSessionHandler.create(authProvider));

		router.route(routePath).handler(this.authHandler);// BasicAuthHandler.create(authProvider, "Please Provide Valid Credentials" ));
		router.route(routePath).handler(requestHandler);// ;
		router.route(routePath).handler(TemplateStaticHandler.create().addVariables(variables).setCachingEnabled(false).setWebRoot(webroot));
	}

	void addPostRoute(String routePath, Handler<RoutingContext> requestHandler) {
		router.route(routePath).handler(CookieHandler.create());
		router.route(routePath).handler(BodyHandler.create().setBodyLimit(Integer.MAX_VALUE));
		router.route(routePath).handler(SessionHandler.create(LocalSessionStore.create(vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		router.route(routePath).handler(UserSessionHandler.create(authProvider));

		//router.post(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		router.post(routePath).handler(requestHandler);
	}

	Session getSession(String sessionId) {
		Session session = this.comms.activeSessions.get(sessionId);
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

	TechSession createTechSession(Session webSession) {
		TechSession unknown = new TechSession("none", new TechUserDetails("<unknown>"));
		if (null == webSession) {
			return unknown;
		} else {
			UserHolder holder = webSession.get("__vertx.userHolder");
			if (holder != null && holder.user !=null) {
				String username = holder.user.principal().getString("username");
				TechSession ts = new TechSession(webSession.id(), new TechUserDetails(username));
				return ts;
			} else {
				return unknown;
			}

		}
	}

	// should maybe move the authentication into its own ?Authenticator class
	public void requestLogin(TechSession techSession, String username, String password) {
		Session session = this.getSession(techSession.getId());
		JsonObject authInfo = new JsonObject();
		authInfo.put("username", username);
		authInfo.put("password", password);
		this.authProvider.authenticate(authInfo, res -> {
			if (res.succeeded()) {
				User u = res.result();
				UserHolder holder = session.get("__vertx.userHolder");
				if (holder != null) {
					holder.context.setUser(u);
					holder.user = u;
					session.put("__vertx.userHolder", holder);
				} else {
					holder = new UserHolder(null);
					holder.user = u;
					session.put("__vertx.userHolder", holder);
				}
				TechSession newTs = new TechSession(techSession.getId(), new TechUserDetails(u.principal().getString("username")));
				this.ws.portGui().out(IAuthenticatorNotification.class).notifyAuthenticationSuccess(newTs);
			} else {
				this.ws.portGui().out(IAuthenticatorNotification.class).notifyAuthenticationFailure(techSession, "Authentication Failed");
			}
		});
	}

	public void requestLogout(TechSession techSession) {
		Session session = this.getSession(techSession.getId());
		session.put("__vertx.userHolder", null);
		this.ws.portGui().out(IAuthenticatorNotification.class).notifyAuthenticationCleared(techSession);
	}

	// -------------------- Verticle -----------------
	MyAuthHandler authHandler;
	AuthProvider authProvider;
	Vertx vertx;
	Router router;
	ClientServerComms comms;

	@Override
	public Vertx getVertx() {
		return this.vertx;
	}

	@Override
	public void init(Vertx vertx, Context context) {
		this.vertx = vertx;
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		router = Router.router(vertx);
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		router.route().handler(UserSessionHandler.create(authProvider));

		ShiroAuthOptions authOpts = new ShiroAuthOptions();
		JsonObject config = new JsonObject();
		config.put("properties_path", "classpath:auth.properties");
		authOpts.setConfig(config);
		authOpts.setType(ShiroAuthRealmType.PROPERTIES);
		this.authProvider = authOpts.createProvider(vertx);
		this.authHandler = new MyAuthHandler(authProvider);

		this.comms = new ClientServerComms(vertx, this.router, this.authProvider, "/eventbus");
//		this.comms.addOutboundAddress("Gui.setTitle");
//		this.comms.addOutboundAddress("Gui.switchToScene");
//		this.comms.addOutboundAddress("Gui.addElement");
//		this.comms.addOutboundAddress("Gui.requestRecieveEvent");
//		this.comms.addOutboundAddress("Gui.setText");
//
//		this.comms.addOutboundAddress("Canvas.addChild");
//		this.comms.addOutboundAddress("Canvas.addChildToParent");
//		this.comms.addOutboundAddress("Canvas.relocate");
//		this.comms.addOutboundAddress("Canvas.resize");
//		this.comms.addOutboundAddress("Canvas.transform");
//		this.comms.addOutboundAddress("Canvas.setStartAnchor");
//		this.comms.addOutboundAddress("Canvas.setEndAnchor");
//
//		this.comms.addInboundAddress("handleEvent");

		router.route(ws.getTestPath()).handler(rc -> {
			rc.response().putHeader("content-type", "text/html").end("<h1>Test</h1>");
		});

		router.route(ws.getJsPath()+"/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("js"));

		router.route(ws.getDownloadPath()+"/:filename").handler(rc -> {
			String filename = rc.request().getParam("filename");
			Buffer buffer = Buffer.buffer();
			ws.portGui().out(IGuiNotification.class).notifyDowloadRequest(createTechSession(rc.session()), filename, new IGuiCallback() {

				@Override
				public void success(Object result) {
					byte[] bytes = (byte[]) result;
					buffer.appendBytes(bytes);
					rc.response().end(buffer);
				}

				@Override
				public void error(Exception ex) {
					rc.response().end(ex.getMessage());
				}
			});

			rc.response().putHeader("content-type", "download");
		});

		this.addPostRoute(ws.getUploadPath(), rc -> {
			FileUpload fu = rc.fileUploads().iterator().next();

			ws.portGui().out(IGuiNotification.class).notifyUpload(createTechSession(rc.session()), fu.uploadedFileName());

		});

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(this.port);

		this.ws.portGui().out(IGuiNotification.class).notifyReady();

		startFuture.complete();
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		stopFuture.complete();
	}

}
