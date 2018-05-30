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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import io.vertx.ext.web.handler.impl.HttpStatusException;

public class MyAuthHandler extends AuthHandlerImpl {

    private final String loginRedirectURL;
    private final String rootPath;

    public MyAuthHandler(final MyAuthProvider authProvider, final String loginRedirectURL, final String rootPath) {
        super(authProvider);
        this.loginRedirectURL = loginRedirectURL;
        this.rootPath = rootPath;
    }

    public MyAuthProvider getProvider() {
        return (MyAuthProvider) super.authProvider;
    }

    @Override
    public void handle(final RoutingContext ctx) {
        final String oldSessionId = ctx.session().id();
        super.handle(ctx);
        // if authenticated, tell provider about new session id, it probably changed.
        final User user = ctx.user();
        if (null != user) {
            this.getProvider().clearAuthentication(oldSessionId);
            this.getProvider().addAuthentication(ctx.session().id(), user.principal().getString("username"));
        }
    }

    // @Override
    // public void handle(final RoutingContext ctx) {
    // try {
    // User user = ctx.user();
    // if (user != null) {
    // // Already logged in, just authorise
    // this.authorize(user, authZ -> {
    // if (authZ.failed()) {
    // this.processException(ctx, authZ.cause());
    // return;
    // }
    // // success, allowed to continue
    // ctx.next();
    // });
    // } else {
    // this.getProvider().authenticate(ctx);
    // user = ctx.user();
    // if (user == null) {
    // // Now redirect to the login url - we'll get redirected back here after successful login
    // ctx.session().put("originalUrl", ctx.request().absoluteURI());
    // ctx.response().putHeader("location", this.loginRedirectURL).setStatusCode(302).end();
    // } else {
    // this.authorize(user, authZ -> {
    // if (authZ.failed()) {
    // this.processException(ctx, authZ.cause());
    // return;
    // }
    // // success, allowed to continue
    // ctx.next();
    // });
    // }
    // }
    // } catch (final Exception e) {
    // // TODO: this should really be logged !
    // System.out.println(e.getMessage());
    // e.printStackTrace();
    // }
    //
    // }

    @Override
    public void parseCredentials(final RoutingContext context, final Handler<AsyncResult<JsonObject>> handler) {
        final Session session = context.session();
        if (session != null) {
            final JsonObject json = new JsonObject();
            json.put("sessionId", context.session().id());

            this.getProvider().authenticate(json, (res) -> {
                if (res.succeeded()) {
                    handler.handle(Future.succeededFuture(json));
                } else {
                    context.session().put("originalUrl", context.request().absoluteURI());
                    handler.handle(Future.failedFuture(new HttpStatusException(302, this.loginRedirectURL)));
                }
            });
        } else {
            handler.handle(Future.failedFuture("No session - did you forget to include a SessionHandler?"));
        }

        // try {
        // // final User user = context.user();
        // // if (user != null) {
        // // // this.getProvider().authenticate(context);
        // // handler.handle(Future.succeededFuture());
        // // } else {
        // final Session session = context.session();
        // if (session != null) {
        // this.getProvider().authenticate(context);
        // final User user = context.user();
        // if (user == null) {
        // context.session().put("originalUrl", context.request().absoluteURI());
        // handler.handle(Future.failedFuture(new HttpStatusException(302, this.loginRedirectURL)));
        // } else {
        // handler.handle(Future.succeededFuture());
        // }
        // } else {
        // handler.handle(Future.failedFuture("No session - did you forget to include a SessionHandler?"));
        // }
        // // }
        // // final User user = context.user();
        // // if (user != null) {
        // // // Already logged in, just authorise
        // // this.authorise(user, context);
        // // } else {
        // // this.getProvider().authenticate(context);
        // // final User user = context.user();
        // // if (user == null) {
        // // // Now redirect to the login url - we'll get redirected back here after successful login
        // // context.session().put("originalUrl", context.request().absoluteURI());
        // // handler.handle(Future.failedFuture(new HttpStatusException(302, this.loginRedirectURL)));
        // // } else {
        // // this.authorise(user, context);
        // // }
        // // }
        // } catch (final Exception e) {
        // // TODO: this should really be logged !
        // System.out.println(e.getMessage());
        // e.printStackTrace();
        // }
    }

}
