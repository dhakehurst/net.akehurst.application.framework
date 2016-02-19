package net.akehurst.requirements.management.technology.gui.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class Auth_Test extends AbstractVerticle {

	  // Convenience method so you can run it in your IDE
	  public static void main(String[] args) {
		  Vertx vertx = Vertx.vertx();
		  vertx.deployVerticle(new Auth_Test());
	  }

	  @Override
	  public void start() throws Exception {

	    Router router = Router.router(vertx);

	    // We need cookies, sessions and request bodies
	    router.route().handler(CookieHandler.create());
	    router.route().handler(BodyHandler.create());
	    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

	    // Simple auth service which uses a properties file for user/role info
	    AuthProvider authProvider = ShiroAuth.create(vertx, ShiroAuthRealmType.PROPERTIES, new JsonObject());

	    // We need a user session handler too to make sure the user is stored in the session between requests
	    router.route().handler(UserSessionHandler.create(authProvider));

	    // Any requests to URI starting '/private/' require login
	    router.route("/private/*").handler(rc -> {
	        Session session = rc.session();
	        if (session != null) {
	          User user = rc.user();
	          if (user != null) {
	            // Already logged in, just authorise
	            // authorise(user, context);
	        	  rc.next();
	          } else {
	            // Now redirect to the login url - we'll get redirected back here after successful login
	            //session.put(returnURLParam, rc.request().path());
	            rc.response().putHeader("location", "/loginhandler").setStatusCode(302).end();
	        	//  rc.response().end();
	          }
	        } else {
	          rc.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
	        }			
		});

	    // Serve the static private pages from directory 'private'
	    router.route("/private/*").handler(rc -> {
			rc.response().putHeader("content-type", "text/html").end("<h1>Private</h1>");
		});

	    // Handles the actual login
	    router.route("/loginhandler").handler(rc -> {
			rc.response().putHeader("content-type", "text/html").end("<h1>Login handler</h1>");
		});

	    // Implement logout
	    router.route("/logout").handler(context -> {
	      context.clearUser();
	      // Redirect back to the index page
	      context.response().putHeader("location", "/").setStatusCode(302).end();
	    });

	    // Serve the non private static pages
	    router.route().handler(rc -> {
			rc.response().putHeader("content-type", "text/html").end("<h1>Public</h1>");
		});

	    vertx.createHttpServer().requestHandler(router::accept).listen(9999);
	  }
	}