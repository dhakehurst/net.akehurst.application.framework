package net.akehurst.application.framework.technology.gui.vertx;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.UserHolder;
import net.akehurst.application.framework.technology.interfaceGui.GuiException;

public class MyAuthProvider implements AuthProvider {

	public MyAuthProvider() {
		this.authenticated = new HashMap<>();
	}

	private final Map<String, String> authenticated;

	@Override
	public void authenticate(final JsonObject authInfo, final Handler<AsyncResult<User>> resultHandler) {
		resultHandler.handle(Future.succeededFuture(new AbstractUser() {
			AuthProvider authProvider;

			@Override
			public JsonObject principal() {
				return new JsonObject().put("username", "user");
			}

			@Override
			public void setAuthProvider(final AuthProvider value) {
				this.authProvider = value;
			}

			@Override
			protected void doIsPermitted(final String permission, final Handler<AsyncResult<Boolean>> resultHandler) {
				resultHandler.handle(Future.succeededFuture(true));
			}
		}));
	}

	public void addAuthentication(final String sessionId, final String username) {
		this.authenticated.put(sessionId, username);
	}

	public void clearAuthentication(final String sessionId) throws GuiException {
		this.authenticated.remove(sessionId);
	}

	public void authenticate(final RoutingContext context) {
		final Session session = context.session();
		if (session != null) {
			final String username = this.authenticated.get(session.id());
			if (null == username) {
				final UserHolder holder = session.get("__vertx.userHolder");
				if (null != holder) {
					session.remove("__vertx.userHolder");
				}
			} else {

				UserHolder holder = session.get("__vertx.userHolder");
				if (null == holder) {
					holder = new UserHolder(context);
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
			}
		} else {
			context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
		}
	}

}
