package net.akehurst.application.framework.technology.gui.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class MyAuthProvider implements AuthProvider {

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

}
