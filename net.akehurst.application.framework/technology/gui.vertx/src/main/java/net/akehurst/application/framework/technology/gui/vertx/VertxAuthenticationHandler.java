package net.akehurst.application.framework.technology.gui.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractActiveSignalProcessingObject;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

public class VertxAuthenticationHandler extends AbstractActiveSignalProcessingObject implements IAuthenticatorRequest {

	public VertxAuthenticationHandler(final String afId) {
		super(afId);
		// TODO: shouldn't create another Vertex really !
		this.vertx = Vertx.vertx();

		// TODO: make this configurable
		final ShiroAuthOptions authOpts = new ShiroAuthOptions();
		final JsonObject config = new JsonObject();
		config.put("properties_path", "classpath:auth.properties");
		authOpts.setConfig(config);
		authOpts.setType(ShiroAuthRealmType.PROPERTIES);
		this.authProvider = authOpts.createProvider(this.vertx);
	}

	IAuthenticatorNotification authenticatorNotification;
	Vertx vertx;
	AuthProvider authProvider;

	@Override
	public void requestLogin(final UserSession session, final String username, final String password, final String encoding) {
		// final Session sess = this.getSession(session.getId());
		final JsonObject authInfo = new JsonObject();
		authInfo.put("username", username);
		authInfo.put("password", password);
		this.authProvider.authenticate(authInfo, res -> {
			if (res.succeeded()) {
				// final User u = res.result();
				// UserHolder holder = sess.get("__vertx.userHolder");
				// if (holder != null) {
				// holder.context.setUser(u);
				// holder.user = u;
				// sess.put("__vertx.userHolder", holder);
				// } else {
				// holder = new UserHolder(null);
				// holder.user = u;
				// sess.put("__vertx.userHolder", holder);
				// }
				final UserSession newTs = new UserSession(session.getId(), new UserDetails(username));
				this.authenticatorNotification.notifyAuthenticationSuccess(newTs);
			} else {
				this.authenticatorNotification.notifyAuthenticationFailure(session, "Authentication Failed");
			}
		});
	}

	@Override
	public void requestLogout(final UserSession session) {
		// final Session session = this.getSession(techSession.getId());
		// session.put("__vertx.userHolder", null);
		this.authenticatorNotification.notifyAuthenticationCleared(new UserSession(session.getId(), null));
	}

}
