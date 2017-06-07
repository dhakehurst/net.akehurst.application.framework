package net.akehurst.application.framework.technology.gui.vertx;

import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.impl.UserHolder;

//TODO: user the vertx version and fix the auth handling to work with the latest vertx version
public class MyUserHandler implements UserSessionHandler {

	private static final String SESSION_USER_HOLDER_KEY = "__vertx.userHolder";

	@Override
	public void handle(final RoutingContext routingContext) {
		final Session session = routingContext.session();
		if (session != null) {
			User user = null;
			final UserHolder holder = session.get(MyUserHandler.SESSION_USER_HOLDER_KEY);
			if (holder != null) {
				final RoutingContext prevContext = holder.context;
				if (prevContext != null) {
					user = prevContext.user();
				} else if (holder.user != null) {
					user = holder.user;
					holder.context = routingContext;
					holder.user = null;
				}
				holder.context = routingContext;
			} else {

				if (routingContext.user() != null) {
					session.put(MyUserHandler.SESSION_USER_HOLDER_KEY, new UserHolder(routingContext));
				}

			}
			if (user != null) {
				routingContext.setUser(user);
			}
		}
		routingContext.next();
	}

}
