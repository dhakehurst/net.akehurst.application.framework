package net.akehurst.requirements.management.technology.gui.vertx.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.Router;

public class test_VertxOAuth {

    private HttpServer server;
    protected Vertx vertx;
    private Router router;

    @Before
    public void setup() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        this.vertx = Vertx.vertx();
        this.router = Router.router(this.vertx);

        this.server = this.vertx.createHttpServer().requestHandler(this.router::accept).listen(8080, ready -> {
            if (ready.failed()) {
                throw new RuntimeException(ready.cause());
            }
            // ready
            latch.countDown();
        });

        latch.await();
    }

    @Ignore
    @Test
    public void t() throws InterruptedException {

        final String clientId = "425940381741-pucip8u310q19abnnp3t8lfurjs5tse6.apps.googleusercontent.com";
        final String clientSecret = "_1256nHEyTKiumHCqvKg1m62";
        final String site = "https://accounts.google.com";
        final String tokenPath = "https://www.googleapis.com/oauth2/v3/token";
        final String authorisationPath = "/o/oauth2/auth";
        final String scopes = "https://www.googleapis.com/auth/spreadsheets.readonly";

        final OAuth2ClientOptions opts = new OAuth2ClientOptions();
        opts.setClientID(clientId);
        opts.setClientSecret(clientSecret);
        opts.setSite(site);
        opts.setTokenPath(tokenPath);
        opts.setAuthorizationPath(authorisationPath);
        final JsonObject extraParams = new JsonObject();
        opts.setExtraParameters(extraParams);

        final OAuth2Auth oauth2 = OAuth2Auth.create(this.vertx, OAuth2FlowType.AUTH_CODE, opts);

        // when there is a need to access a protected resource or call a protected method,
        // call the authZ url for a challenge
        final JsonObject params = new JsonObject();
        params.put("redirect_uri", "http://localhost:8080/callback");
        params.put("scope", scopes);
        params.put("state", "3(#0/!~");
        params.put("access_type", "offline");
        params.put("approval_prompt", "force");
        final String authorization_uri = oauth2.authorizeURL(params);
        System.out.println(authorization_uri);
        // when working with web application use the above string as a redirect url

        // in this case GitHub will call you back in the callback uri one should now complete the handshake as:
        this.router.route("/callback").handler(rc -> {
            final String code = rc.request().getParam("code");

            // final String code = "xxxxxxxxxxxxxxxxxxxxxxxx"; // the code is provided as a url parameter by github callback call
            final JsonObject cfg = new JsonObject().put("code", code).put("redirect_uri", "http://localhost:8080/callback");
            oauth2.getToken(cfg, res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    rc.response().end(res.cause().getMessage());
                } else {
                    // save the token and continue...
                    final AccessToken tok = res.result();
                    System.out.println(tok.principal().encodePrettily());
                    tok.refresh(rres -> {
                        if (rres.failed()) {
                            rres.cause().printStackTrace();
                            rc.response().end(rres.cause().getMessage());
                        } else {
                            final JsonObject jsonTok = tok.principal();
                            rc.response().end(jsonTok.encodePrettily());
                            // rc.response().end("Granted");
                        }
                    });

                }
            });
        });
        Thread.sleep(1000000);
    }

}
