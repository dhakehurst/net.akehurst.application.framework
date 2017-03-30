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

import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.impl.UserHolder;
import io.vertx.ext.web.sstore.LocalSessionStore;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceGui.DialogIdentity;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.GuiException;
import net.akehurst.application.framework.technology.interfaceGui.IGuiCallback;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class AVerticle implements Verticle {

	public AVerticle(final VertxWebsite ws, final int port) {
		this.port = port;
		this.ws = ws;
	}

	int port;
	VertxWebsite ws;

	void addRoute(final String stagePath, final Handler<RoutingContext> requestHandler, final String webroot, final Map<String, String> variables) {
		final String routePath = stagePath + "*";

		final String sockjsCommsPath = stagePath + this.ws.getSockjsPath() + "*";
		this.comms.addSocksChannel(sockjsCommsPath, (session, channelId, data) -> {
			try {
				switch (channelId) {
					case "IGuiNotification.notifyEventOccured": {
						String stageIdStr = data.getString("stageId");
						stageIdStr = stageIdStr.replace(this.ws.rootPath, "");
						final StageIdentity stageId = new StageIdentity(stageIdStr);
						final SceneIdentity sceneId = new SceneIdentity(data.getString("sceneId"));
						final String dialogIdStr = data.getString("dialogId");
						final DialogIdentity dialogId = null == dialogIdStr ? null : new DialogIdentity(dialogIdStr);
						final String eventType = data.getString("eventType");
						final String elementId = data.getString("elementId");
						final Map<String, Object> eventData = data.getJsonObject("eventData").getMap();

						this.ws.portGui().out(IGuiNotification.class)
								.notifyEventOccured(new GuiEvent(session, new GuiEventSignature(stageId, sceneId, dialogId, elementId, eventType), eventData));
					}
					break;

					default:
					break;
				}
			} catch (final GuiException e) {
				this.ws.logger.log(LogLevel.ERROR, e.getMessage(), e);
			}
		});

		this.router.route(routePath).handler(CookieHandler.create());
		this.router.route(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		this.router.route(routePath)
				.handler(SessionHandler.create(LocalSessionStore.create(this.getVertx())).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		this.router.route(routePath).handler(UserSessionHandler.create(this.authProvider));

		this.router.route(routePath).handler(requestHandler);
		this.router.route(routePath).handler(TemplateStaticHandler.create().addVariables(variables).setCachingEnabled(false).setWebRoot(webroot));
		// this.router.route(routePath).handler(StaticHandler.create().setCachingEnabled(false).setWebRoot(webroot));

		this.ws.logger.log(LogLevel.INFO, "Public path:  " + "http://localhost:" + this.port + stagePath);
	}

	void addAuthenticatedRoute(final String stagePath, final Handler<RoutingContext> requestHandler, final String webroot,
			final Map<String, String> variables) {
		final String routePath = stagePath + "*";

		final String sockjsCommsPath = stagePath + this.ws.getSockjsPath() + "*";
		this.comms.addSocksChannel(sockjsCommsPath, (session, channelId, data) -> {
			try {
				if ("IGuiNotification.notifyEventOccured".equals(channelId)) {
					String stageIdStr = data.getString("stageId");
					stageIdStr = stageIdStr.replace(this.ws.rootPath, "");
					final StageIdentity stageId = new StageIdentity(stageIdStr);
					final SceneIdentity sceneId = new SceneIdentity(data.getString("sceneId"));
					final String dialogIdStr = data.getString("dialogId");
					final DialogIdentity dialogId = null == dialogIdStr ? null : new DialogIdentity(dialogIdStr);
					final String eventType = data.getString("eventType");
					final String elementId = data.getString("elementId");
					final Map<String, Object> eventData = data.getJsonObject("eventData").getMap();
					this.ws.portGui().out(IGuiNotification.class)
							.notifyEventOccured(new GuiEvent(session, new GuiEventSignature(stageId, sceneId, dialogId, elementId, eventType), eventData));
				} else {
					// ??
				}
			} catch (final GuiException e) {
				this.ws.logger.log(LogLevel.ERROR, e.getMessage(), e);
			}
		});

		this.router.route(routePath).handler(CookieHandler.create());
		this.router.route(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		this.router.route(routePath)
				.handler(SessionHandler.create(LocalSessionStore.create(this.getVertx())).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		this.router.route(routePath).handler(UserSessionHandler.create(this.authProvider));

		this.router.route(routePath).handler(this.authHandler);// BasicAuthHandler.create(authProvider, "Please Provide Valid Credentials" ));
		this.router.route(routePath).handler(requestHandler);// ;
		this.router.route(routePath).handler(TemplateStaticHandler.create().addVariables(variables).setCachingEnabled(false).setWebRoot(webroot));

		this.ws.logger.log(LogLevel.INFO, "Protected path:  " + "http://localhost:" + this.port + stagePath);
	}

	void addPostRoute(final String routePath, final Handler<RoutingContext> requestHandler) {
		this.router.route(routePath).handler(CookieHandler.create());
		this.router.route(routePath).handler(BodyHandler.create().setBodyLimit(Integer.MAX_VALUE));
		this.router.route(routePath)
				.handler(SessionHandler.create(LocalSessionStore.create(this.getVertx())).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		this.router.route(routePath).handler(UserSessionHandler.create(this.authProvider));

		// router.post(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		this.router.post(routePath).handler(requestHandler);
	}

	Session getSession(final String sessionId) {
		final Session session = this.comms.activeSessions.get(sessionId);
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

	UserSession createUserSession(final Session webSession) {
		final UserSession unknown = new UserSession("none", new UserDetails("<unknown>"));
		if (null == webSession) {
			return unknown;
		} else {
			final UserHolder holder = webSession.get("__vertx.userHolder");
			if (holder != null && holder.user != null) {
				final String username = holder.user.principal().getString("username");
				final UserSession ts = new UserSession(webSession.id(), new UserDetails(username));
				return ts;
			} else {
				return unknown;
			}

		}
	}

	public void authenticate(final UserSession session) throws GuiException {
		final UserDetails details = session.getUser();
		if (null != details) {
			final String username = details.getName();
			final Session sess = this.getSession(session.getId());
			UserHolder holder = sess.get("__vertx.userHolder");
			if (null == holder) {
				holder = new UserHolder(null);
			}
			final User user = new AbstractUser() {

				@Override
				public void setAuthProvider(final AuthProvider authProvider) {
					// TODO Auto-generated method stub

				}

				@Override
				public JsonObject principal() {
					return new JsonObject().put("username", username);
				}

				@Override
				protected void doIsPermitted(final String permission, final Handler<AsyncResult<Boolean>> resultHandler) {
					// TODO Auto-generated method stub

				}
			};
			holder.user = user;
			if (null != holder.context) {
				holder.context.setUser(user);
			}

		} else {
			throw new GuiException("Cannot authenticate", null);
		}
	}

	// should maybe move the authentication into its own ?Authenticator class
	public void requestLogin(final UserSession session, final String username, final String password) {
		final Session sess = this.getSession(session.getId());
		final JsonObject authInfo = new JsonObject();
		authInfo.put("username", username);
		authInfo.put("password", password);
		this.authProvider.authenticate(authInfo, res -> {
			if (res.succeeded()) {
				final User u = res.result();
				UserHolder holder = sess.get("__vertx.userHolder");
				if (holder != null) {
					holder.context.setUser(u);
					holder.user = u;
					sess.put("__vertx.userHolder", holder);
				} else {
					holder = new UserHolder(null);
					holder.user = u;
					sess.put("__vertx.userHolder", holder);
				}
				final UserSession newTs = new UserSession(session.getId(), new UserDetails(u.principal().getString("username")));
				this.ws.portGui().out(IAuthenticatorNotification.class).notifyAuthenticationSuccess(newTs);
			} else {
				this.ws.portGui().out(IAuthenticatorNotification.class).notifyAuthenticationFailure(session, "Authentication Failed");
			}
		});
	}

	public void requestLogout(final UserSession techSession) {
		final Session session = this.getSession(techSession.getId());
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
	public void init(final Vertx vertx, final Context context) {
		this.vertx = vertx;
	}

	@Override
	public void start(final Future<Void> startFuture) throws Exception {

		this.router = Router.router(this.getVertx());
		// this.router.route(this.ws.getTestPath()).handler(CookieHandler.create());
		// this.router.route(this.ws.getTestPath()).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
		// this.router.route(this.ws.getTestPath()).handler(SessionHandler.create(LocalSessionStore.create(this.vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
		// this.router.route(this.ws.getTestPath()).handler(UserSessionHandler.create(this.authProvider));
		final String testPath = this.ws.getTestPath();
		this.router.route(testPath).handler(rc -> {
			rc.response().putHeader("content-type", "text/html").end("<h1>Test</h1>");
		});
		this.ws.logger.log(LogLevel.INFO, "Test path:  " + "http://localhost:" + this.port + testPath);

		// final ShiroAuthOptions authOpts = new ShiroAuthOptions();
		// final JsonObject config = new JsonObject();
		// config.put("properties_path", "classpath:auth.properties");
		// authOpts.setConfig(config);
		// authOpts.setType(ShiroAuthRealmType.PROPERTIES);
		// this.authProvider = authOpts.createProvider(this.getVertx());
		this.authProvider = new MyAuthProvider();
		this.authHandler = new MyAuthHandler(this.authProvider);

		this.comms = new ClientServerComms(this.getVertx(), this.router, this.authProvider, "/eventbus");

		final String jsPath = this.ws.getJsPath() + "/*";
		this.router.route(jsPath).handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("js"));
		this.ws.logger.log(LogLevel.INFO, "Test path:  " + "http://localhost:" + this.port + jsPath);

		// TODO: replace jsPath with this once all my js code is ported
		final String libPath = "/lib/*";
		this.router.route(libPath).handler(StaticHandler.create("META-INF/resources/webjars"));
		this.ws.logger.log(LogLevel.INFO, "Test path:  " + "http://localhost:" + this.port + libPath);

		final String downloadPath = this.ws.getDownloadPath() + ":filename";
		this.router.route(downloadPath).handler(rc -> {
			final String filename = rc.request().getParam("filename");
			final Buffer buffer = Buffer.buffer();
			this.ws.portGui().out(IGuiNotification.class).notifyDowloadRequest(this.createUserSession(rc.session()), filename, new IGuiCallback() {

				@Override
				public void success(final Object result) {
					final byte[] bytes = (byte[]) result;
					buffer.appendBytes(bytes);
					rc.response().end(buffer);
				}

				@Override
				public void error(final Exception ex) {
					rc.response().end(ex.getMessage());
				}
			});

			rc.response().putHeader("content-type", "download");
		});
		this.ws.logger.log(LogLevel.INFO, "Download path:  " + "http://localhost:" + this.port + downloadPath);

		final String uploadPath = this.ws.getUploadPath();
		this.addPostRoute(uploadPath, rc -> {
			final FileUpload fu = rc.fileUploads().iterator().next();

			this.ws.portGui().out(IGuiNotification.class).notifyUpload(this.createUserSession(rc.session()), fu.uploadedFileName());

		});
		this.ws.logger.log(LogLevel.INFO, "Upload path:  " + "http://localhost:" + this.port + uploadPath);

		final HttpServer server = this.getVertx().createHttpServer();
		server.requestHandler(this.router::accept).listen(this.port);

		this.ws.portGui().out(IGuiNotification.class).notifyReady();

		startFuture.complete();
	}

	@Override
	public void stop(final Future<Void> stopFuture) throws Exception {
		stopFuture.complete();
	}

}
