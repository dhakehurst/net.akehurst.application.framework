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
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.GuiException;
import net.akehurst.application.framework.technology.interfaceGui.IGuiCallback;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class AVerticle implements Verticle {

    public AVerticle(final VertxWebsite ws, final int port) {
        this.port = port;
        this.ws = ws;
        this.register = new HashMap<>();
    }

    private final int port;
    private final VertxWebsite ws;
    private MyAuthProvider authProvider;
    private Vertx vertx;
    private Router router;
    private ClientServerComms comms;
    private final Map<String, IReceiveMessage> register;

    public ClientServerComms getComms() {
        return this.comms;
    }

    public void register(final String channelId, final IReceiveMessage func) {
        this.register.put(channelId, func);
    }

    void addRoute(final boolean authenticated, final String authenticationRedirect, final String stagePath, final boolean frontEndRouting,
            final Handler<RoutingContext> requestHandler, final String webroot, final Map<String, String> variables) {
        final String routePath = stagePath + "*";

        this.comms.addSocksChannel(authenticated, authenticationRedirect, stagePath, this.ws.getSockjsPath(), (session, channelId, data) -> {
            this.ws.logger.log(LogLevel.DEBUG, "received socks message %s %s", channelId, data);
            final IReceiveMessage func = this.register.get(channelId);
            if (null == func) {
                this.ws.logger.log(LogLevel.WARN, "received message on unknown channel %s", channelId);
            } else {
                final Map<String, Object> data1 = data.getMap();
                // FIXME: should use a signal processing object, this should be one
                final Thread t = new Thread(() -> {
                    func.receive(session, channelId, data1);
                });
                t.start();
            }
        });

        this.router.route(routePath).handler(CookieHandler.create());
        this.router.route(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
        this.router.route(routePath)
                .handler(SessionHandler.create(LocalSessionStore.create(this.getVertx())).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
        if (authenticated) {
            this.router.route(routePath).handler(UserSessionHandler.create(this.authProvider));
            // this.router.route(routePath).handler(this.authHandler);// BasicAuthHandler.create(authProvider, "Please Provide Valid Credentials" ));
            final String loginRedirectURL = this.ws.rootPath.isEmpty() ? authenticationRedirect : this.ws.rootPath + authenticationRedirect;
            this.router.route(routePath).handler(new MyAuthHandler(this.authProvider, loginRedirectURL, this.ws.rootPath));
        }
        this.router.route(routePath).handler(requestHandler);// ;
        this.router.route(routePath).handler(TemplateStaticHandler.create().addVariables(variables).setCachingEnabled(false).setWebRoot(webroot));

        if (frontEndRouting) {
            this.router.route(routePath).handler(rc -> {
                // final int s = rc.request().path().lastIndexOf('/');
                // final int s2 = rc.request().path().lastIndexOf('.');
                // final String file = s2 > s ? rc.request().path().substring(s + 1) : "";
                // rc.reroute(stagePath + file);
                rc.reroute(stagePath.substring(0, stagePath.length() - 1));
            });
        }

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

    public void addAuthentication(final UserSession session) throws GuiException {
        final UserDetails details = session.getUser();
        if (null != details) {
            final String username = details.getName();
            this.authProvider.addAuthentication(session.getId(), username);
        } else {
            throw new GuiException("Cannot authenticate", null);
        }
    }

    public void clearAuthentication(final UserSession session) throws GuiException {
        this.authProvider.clearAuthentication(session.getId());
    }

    public java.util.concurrent.Future<String> oauthAuthorise(final UserSession session, final String clientId, final String clientSecret, final String site,
            final String tokenPath, final String authorisationPath, final String scopes) {
        final OAuth2ClientOptions opts = new OAuth2ClientOptions();
        opts.setClientID(clientId);
        opts.setClientSecret(clientSecret);
        opts.setSite(site);
        opts.setTokenPath(tokenPath);
        opts.setAuthorizationPath(authorisationPath);
        // final JsonObject extraParams = new JsonObject();
        // extraParams.put("sessionId", session.getId());
        // opts.setExtraParameters(extraParams);
        final OAuth2Auth oauth2 = OAuth2Auth.create(this.getVertx(), OAuth2FlowType.AUTH_CODE, opts);

        final JsonObject params = new JsonObject();
        final String oauthCallbackPath = this.ws.getOAuthCallbckPath();
        final String redirect_uri = "http://localhost:" + this.port + oauthCallbackPath;
        params.put("redirect_uri", redirect_uri);
        params.put("scope", scopes);
        params.put("state", "3(#0/!~");
        params.put("access_type", "offline");
        params.put("approval_prompt", "force");
        final String authorization_uri = oauth2.authorizeURL(params);

        this.ws.newWindow(session, authorization_uri);

        final JsonObject tokenConfig = new JsonObject();

        tokenConfig.put("redirect_uri", redirect_uri);

        final CompletableFuture<String> result = new CompletableFuture<>();
        this.router.route(oauthCallbackPath).handler(rc -> {
            final String code = rc.request().getParam("code");
            tokenConfig.put("code", code);
            oauth2.getToken(tokenConfig, res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    result.complete(res.cause().getMessage());
                    rc.response().end("Error: " + res.cause().getMessage());
                } else {
                    // save the token and continue...
                    final AccessToken tok = res.result();
                    final JsonObject jsonTok = tok.principal();
                    result.complete(jsonTok.getString("refresh_token"));
                    rc.response().end("Granted");

                }
            });

        });

        return result;
    }

    // -------------------- Verticle -----------------

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
        final String testPath = this.ws.getTestPath();
        this.router.route(testPath).handler(rc -> {
            rc.response().putHeader("content-type", "text/html").end("<h1>Test</h1>");
        });
        this.ws.logger.log(LogLevel.INFO, "Test path:  " + "http://localhost:" + this.port + testPath);

        this.authProvider = new MyAuthProvider();

        this.comms = new ClientServerComms(this.getVertx(), this.router, this.authProvider, this.ws.rootPath, "/eventbus");

        final String jsPath = this.ws.getJsPath() + "/*";
        this.router.route(jsPath).handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("js"));
        this.ws.logger.log(LogLevel.INFO, "JS path:  " + "http://localhost:" + this.port + jsPath);

        // TODO: replace jsPath with this once all my js code is ported
        final String libPath = this.ws.getLibPath() + "/*";
        final String libClassPath = this.ws.getLibClassPath();
        this.router.route(libPath).handler(StaticHandler.create(libClassPath));
        this.ws.logger.log(LogLevel.INFO, "Lib path:  " + "http://localhost:" + this.port + libPath);

        final String downloadPath = this.ws.getDownloadPath() + ":filename";

        this.router.route(downloadPath).handler(CookieHandler.create());
        this.router.route(downloadPath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
        this.router.route(downloadPath)
                .handler(SessionHandler.create(LocalSessionStore.create(this.getVertx())).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
        this.router.route(downloadPath).handler(UserSessionHandler.create(this.authProvider));
        this.router.route(downloadPath).handler(rc -> {
            final Map<String, List<String>> params = new HashMap<>();
            for (final Map.Entry<String, String> me : rc.request().params().entries()) {
                List<String> list = params.get(me.getKey());
                if (null == list) {
                    list = new ArrayList<>();
                    params.put(me.getKey(), list);
                }
                list.add(me.getValue());
            }
            final Buffer buffer = Buffer.buffer();
            this.ws.portGui().out(IGuiNotification.class).notifyDowloadRequest(this.comms.createUserSession(rc.session(), rc.user()), params,
                    new IGuiCallback() {

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
            final UserSession session = this.comms.createUserSession(rc.session(), rc.user());
            final IReceiveMessage rec = this.register.get("upload");
            for (final FileUpload f : rc.fileUploads()) {
                final Map<String, Object> data = new HashMap<>();
                data.put("uploadedFilename", f.uploadedFileName());
                rec.receive(session, "upload", data);
                // this.ws.portGui().out(IGuiNotification.class).notifyUpload(session, f.uploadedFileName());
            }
            rc.response().end();
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
