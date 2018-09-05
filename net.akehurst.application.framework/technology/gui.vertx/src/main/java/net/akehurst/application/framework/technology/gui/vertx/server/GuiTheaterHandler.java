package net.akehurst.application.framework.technology.gui.vertx.server;

import java.util.HashMap;
import java.util.Map;

import org.hjson.JsonArray;
import org.jooq.lambda.function.Consumer3;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import net.akehurst.application.framework.common.annotations.declaration.ExternalConnection;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.IdentifiableObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.ActiveSignalProcessingObjectAbstract;
import net.akehurst.application.framework.technology.gui.api.GuiTheaterNotification;
import net.akehurst.application.framework.technology.gui.api.GuiTheaterRequest;
import net.akehurst.application.framework.technology.gui.vertx.IpPort;
import net.akehurst.application.framework.technology.gui.vertx.TemplateStaticHandler;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventType;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class GuiTheaterHandler extends ActiveSignalProcessingObjectAbstract implements GuiTheaterRequest {

    @ServiceReference
    public ILogger logger;

    @IdentifiableObjectInstance
    private WebsocketComms comms;

    @ConfiguredValue(defaultValue = "")
    public String rootPath;

    @ConfiguredValue(defaultValue = "websocket")
    public String websocketSegment;

    @CommandLineArgument(description = "Override the default (9999) port")
    @ConfiguredValue(defaultValue = "9999")
    private IpPort port;

    @ExternalConnection
    private GuiTheaterNotification guiTheaterNotification;

    private Vertx vertx;
    private Router router;

    public GuiTheaterHandler(final String afId) {
        super(afId);
    }

    private String getWebsocketPath(final String stageId) {
        return this.getNormalisedRootPath() + stageId + "/" + this.websocketSegment;
    }

    private String getNormalisedRootPath() {
        return 0 == this.rootPath.length() ? "/" : this.rootPath + "/";
    }

    private void addRoute(final boolean authenticated, final String authenticationRedirect, final String stagePath, final boolean frontEndRouting,
            final Handler<RoutingContext> requestHandler, final String webroot, final Map<String, String> variables) {
        final String routePath = stagePath + "*";

        this.router.route(routePath).handler(CookieHandler.create());
        this.router.route(routePath).handler(BodyHandler.create().setBodyLimit(50 * 1024 * 1024));
        this.router.route(routePath)
                .handler(SessionHandler.create(LocalSessionStore.create(this.vertx)).setCookieHttpOnlyFlag(false).setCookieSecureFlag(false));
        // if (authenticated) {
        // this.router.route(routePath).handler(UserSessionHandler.create(this.authProvider));
        // // this.router.route(routePath).handler(this.authHandler);// BasicAuthHandler.create(authProvider, "Please Provide Valid Credentials" ));
        // final String loginRedirectURL = this.ws.rootPath.isEmpty() ? authenticationRedirect : this.ws.rootPath + authenticationRedirect;
        // this.router.route(routePath).handler(new MyAuthHandler(this.authProvider, loginRedirectURL, this.ws.rootPath));
        // }
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

        this.logger.log(LogLevel.INFO, "Protected path:  " + "http://localhost:" + this.port.asPrimitive() + stagePath);
    }

    private void initialise() {
        this.vertx = Vertx.vertx();
        this.router = Router.router(this.vertx);

        this.vertx.createHttpServer() //
                .websocketHandler(this.comms) //
                .requestHandler(this.router::accept) //
                .listen(this.port.asPrimitive());

        this.guiTheaterNotification.notifyTheaterReady(new UserSession("<gui-initialised>", null, null));
    }

    // --- ActiveSignalProcessingObjectAbstract ---

    @Override
    public void afStart() {
        this.initialise();
        super.afStart();
    }

    // --- GuiTheater ---
    @Override
    public void requestCreateStage(final UserSession session, final StageIdentity stageId, final String contentPath, final StageIdentity authenticationStageId,
            final SceneIdentity authenticationSceneId, final boolean frontEndRouting) {

        try {
            final String websocketFullPath = this.getWebsocketPath(stageId.asPrimitive());
            final Map<String, String> variables = new HashMap<>();
            variables.put("af.rootPath", this.rootPath);
            variables.put("af.stageId", stageId.asPrimitive());
            variables.put("af.websocketPath", websocketFullPath);
            final String stagePath = stageId.asPrimitive().isEmpty() ? "" : stageId.asPrimitive() + "/";

            String str = contentPath;
            if (str.endsWith("/")) {
                str = str.substring(0, str.length() - 1);
            }
            if (str.startsWith("/")) {
                str = str.substring(1, str.length());
            }
            final String webroot = str;
            final String routePath = this.getNormalisedRootPath() + stagePath;
            if (null == authenticationStageId || null == authenticationSceneId) {
                this.addRoute(false, null, routePath, frontEndRouting, rc -> {
                    // this.verticle.getComms().activeSessions.put(rc.session().id(), rc.session());
                    final User u = rc.user();
                    final String path = rc.normalisedPath();
                    this.logger.log(LogLevel.TRACE, "%s requested", path);
                    rc.next();
                }, webroot, variables);
            } else {
                final String authenticationRedirect = "/" + authenticationStageId.asPrimitive() + "/" + authenticationSceneId.asPrimitive() + "/";
                this.addRoute(true, authenticationRedirect, routePath, frontEndRouting, rc -> {
                    // this.verticle.getComms().activeSessions.put(rc.session().id(), rc.session());
                    final User u = rc.user();
                    final String path = rc.normalisedPath();
                    this.logger.log(LogLevel.TRACE, "%s requested by user %s", path, null == u ? "null" : u.principal());
                    rc.next();
                }, webroot, variables);

            }

            final GuiEventSignature signature = new GuiEventSignature(stageId, null, null, null, GuiEventType.STAGE_CREATED);
            final Map<String, Object> eventData = new HashMap<>();
            final GuiEvent event = new GuiEvent(null, signature, eventData);

            this.comms.connect(websocketFullPath);

            this.guiTheaterNotification.notifyStageCreated(session, stageId);

        } catch (final Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void requestSend(final UserSession session, final String channelId, final JsonArray args) {
        this.comms.send(session, channelId, args);
    }

    @Override
    public void requestReceive(final String channelId, final Consumer3<UserSession, String, JsonArray> handler) {
        this.comms.receive(channelId, handler);
    }

}
