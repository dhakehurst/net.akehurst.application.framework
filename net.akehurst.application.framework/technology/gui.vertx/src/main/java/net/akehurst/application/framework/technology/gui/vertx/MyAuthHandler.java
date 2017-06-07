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

import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;

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
	public void handle(final RoutingContext context) {

		User user = context.user();
		if (user != null) {
			// Already logged in, just authorise
			this.authorise(user, context);
		} else {
			this.getProvider().authenticate(context);
			user = context.user();
			if (user == null) {
				// Now redirect to the login url - we'll get redirected back here after successful login
				this.decodeOriginalUrl(context.session(), context.request().absoluteURI());
				context.response().putHeader("location", this.loginRedirectURL).setStatusCode(302).end();
			} else {
				this.authorise(user, context);
			}
		}

	}

	private void decodeOriginalUrl(final Session session, final String originalUrlStr) {
		session.put("originalUrl", originalUrlStr);
		// TODO: find a way to decode the url into a stage,scene pair ??
		// String stageSceneStr = originalUrlStr.replace(this.rootPath, "");
		// int sepIndex = stageSceneStr.indexOf('/');
		// String stageIdStr =stageSceneStr.substring(stageSceneStr.inde);
		// session.put("originalStageId", stageIdStr);
		// session.put("originalSceneId", sceneIdStr);
	}

}
