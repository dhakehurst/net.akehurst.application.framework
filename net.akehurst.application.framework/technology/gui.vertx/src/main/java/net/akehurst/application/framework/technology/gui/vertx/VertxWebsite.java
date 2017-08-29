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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jooq.lambda.tuple.Tuple3;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.gui.vertx.elements.VertxGuiDialogProxy;
import net.akehurst.application.framework.technology.gui.vertx.elements.VertxGuiSceneProxy;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;
import net.akehurst.application.framework.technology.interfaceGui.DialogIdentity;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventType;
import net.akehurst.application.framework.technology.interfaceGui.GuiException;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequestMessage;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class VertxWebsite extends AbstractComponent implements IGuiRequest {

	static final String EVENT_STAGE_CREATED = "IGuiNotification.notifyStageCreated";
	static final String EVENT_STAGE_CLOSED = "IGuiNotification.notifyStageClosed";
	static final String EVENT_SCENE_LOADED = "IGuiNotification.notifySceneLoaded";

	public VertxWebsite(final String objectId) {
		super(objectId);
	}

	@ServiceReference
	private IApplicationFramework af;

	@ServiceReference
	public ILogger logger;

	@ConfiguredValue(defaultValue = "")
	public String rootPath;

	@ConfiguredValue(defaultValue = "test/")
	private String testPath;

	@ConfiguredValue(defaultValue = "download/")
	private String downloadPath;

	@ConfiguredValue(defaultValue = "upload/")
	private String uploadPath;

	@ConfiguredValue(defaultValue = "oauth/callback")
	private String oauthCallbackPath;

	@ConfiguredValue(defaultValue = "js")
	private String jsPath;

	@ConfiguredValue(defaultValue = "lib", description = "The path to which webjar libraries are maped, i.e. js libs on the classpath")
	private String libPath;

	@ConfiguredValue(defaultValue = "META-INF/resources/webjars", description = "The class path for finding js libraries, the 'libPath' is mapped to this.")
	private String libClassPath;

	@CommandLineArgument(description = "Override the default (9999) port")
	@ConfiguredValue(defaultValue = "9999")
	private IpPort port;

	private AVerticle verticle;

	private Map<SceneIdentity, IGuiScene> scenes;

	public String getTestPath() {
		return this.getNormalisedRootPath() + this.testPath;
	}

	public String getNormalisedRootPath() {
		return 0 == this.rootPath.length() ? "/" : this.rootPath + "/";
	}

	public String getDownloadPath() {
		return this.getNormalisedRootPath() + this.downloadPath;
	}

	public String getUploadPath() {
		return this.getNormalisedRootPath() + this.uploadPath;
	}

	public String getJsPath() {
		return this.getNormalisedRootPath() + this.jsPath;
	}

	public String getLibPath() {
		return this.getNormalisedRootPath() + this.libPath;
	}

	public String getLibClassPath() {
		return this.libClassPath;
	}

	public String getSockjsPath() {
		// this value is hard coded in companion javascript, they must match
		return "sockjs/";
	}

	public String getOAuthCallbckPath() {
		return this.getNormalisedRootPath() + this.oauthCallbackPath;
	}

	// --- Component ---
	@Override
	public void afConnectParts() {
	}

	@Override
	public void afRun() {
		this.verticle = new AVerticle(this, this.port.asPrimitive());
		final Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(this.verticle);

		this.verticle.register("IGuiNotification.notifyEventOccured", (session, channelId, data) -> {
			try {
				String stageIdStr = (String) data.get("stageId");
				stageIdStr = stageIdStr.replace(this.rootPath, "");
				final StageIdentity stageId = new StageIdentity(stageIdStr);
				final SceneIdentity sceneId = new SceneIdentity((String) data.get("sceneId"));
				final String dialogIdStr = (String) data.get("dialogId");
				final DialogIdentity dialogId = null == dialogIdStr ? null : new DialogIdentity(dialogIdStr);
				final GuiEventType eventType = this.convertToGuiEvent((String) data.get("eventType"));
				final String elementId = (String) data.get("elementId");
				final Map<String, Object> eventData = (Map<String, Object>) data.get("eventData");
				this.portGui().out(IGuiNotification.class)
						.notifyEventOccured(new GuiEvent(session, new GuiEventSignature(stageId, sceneId, dialogId, elementId, eventType), eventData));
			} catch (final GuiException e) {
				this.logger.log(LogLevel.ERROR, e.getMessage(), e);
			}
		});
	}

	// --------- IGuiRequest ---------
	@Override
	public void addAuthentication(final UserSession session) throws GuiException {
		this.verticle.addAuthentication(session);
	}

	@Override
	public void clearAuthentication(final UserSession session) throws GuiException {
		this.verticle.clearAuthentication(session);
	}

	@Override
	public java.util.concurrent.Future<String> oauthAuthorise(final UserSession session, final String clientId, final String clientSecret, final String site,
			final String tokenPath, final String authorisationPath, final String scopes) {
		return this.verticle.oauthAuthorise(session, clientId, clientSecret, site, tokenPath, authorisationPath, scopes);
	}

	@Override
	public void onRequest(final UserSession session, final String channelId, final IGuiRequestMessage func) {
		// TODO: maybe session should be used as part of register!
		this.verticle.register(channelId, (session1, channelId1, data) -> {
			final Object result = func.receive(session1, channelId1, data);
			final JsonObject jsonData = JsonObject.mapFrom(result);
			this.verticle.getComms().send(session, channelId, jsonData);
		});
	}

	@Override
	public void requestRecieveEvent(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final GuiEventType eventType) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("elementId", elementId);
		final String jsEventType = this.convertToJsEvent(eventType);
		data.put("eventType", jsEventType);

		this.verticle.getComms().send(session, "Gui.requestRecieveEvent", data);

	}

	public GuiEventType convertToGuiEvent(final String eventType) {
		switch (eventType) {
			case "click":
				return GuiEventType.CLICK;
			case "change":
				return GuiEventType.CHANGE;
			case EVENT_SCENE_LOADED:
				return GuiEventType.SCENE_LOADED;
			case EVENT_STAGE_CLOSED:
				return GuiEventType.STAGE_CLOSED;
			case EVENT_STAGE_CREATED:
				return GuiEventType.STAGE_CREATED;
			default:
			break;
		}
		return null;
	}

	private String convertToJsEvent(final GuiEventType eventType) {
		switch (eventType) {
			case CLICK:
				return "click";
			case CHANGE:
				return "change";
			case SCENE_LOADED:
				return VertxWebsite.EVENT_SCENE_LOADED;
			case STAGE_CLOSED:
				return VertxWebsite.EVENT_STAGE_CLOSED;
			case STAGE_CREATED:
				return VertxWebsite.EVENT_STAGE_CREATED;
			case UPLOAD:
				return "upload";
			default:
			break;
		}
		return null;
	}

	@Override
	public void createStage(final StageIdentity stageId, final String contentRoot, final StageIdentity authenticationStageId,
			final SceneIdentity authenticationSceneId) {
		try {
			final Map<String, String> variables = new HashMap<>();
			variables.put("rootPath", this.rootPath);
			final String js = this.getJsPath().startsWith("/") ? this.getJsPath().substring(1) : this.getJsPath();
			variables.put("jsPath", js);
			variables.put("stageId", stageId.asPrimitive());

			final String stagePath = stageId.asPrimitive().isEmpty() ? "" : stageId.asPrimitive() + "/";

			String str = contentRoot;
			if (str.endsWith("/")) {
				str = str.substring(0, str.length() - 1);
			}
			final String webroot = str.substring(str.lastIndexOf('/') + 1);
			final String routePath = this.getNormalisedRootPath() + stagePath;
			if (null == authenticationStageId || null == authenticationSceneId) {
				this.verticle.addRoute(false, null, routePath, rc -> {
					this.verticle.getComms().activeSessions.put(rc.session().id(), rc.session());
					final User u = rc.user();
					final String path = rc.normalisedPath();
					this.logger.log(LogLevel.TRACE, "%s requested", path);
					rc.next();
				}, webroot, variables);
			} else {
				final String authenticationRedirect = "/" + authenticationStageId.asPrimitive() + "/" + authenticationSceneId.asPrimitive() + "/";
				this.verticle.addRoute(true, authenticationRedirect, routePath, rc -> {
					this.verticle.getComms().activeSessions.put(rc.session().id(), rc.session());
					final User u = rc.user();
					final String path = rc.normalisedPath();
					this.logger.log(LogLevel.TRACE, "%s requested by user %s", path, null == u ? "null" : u.principal());
					rc.next();
				}, webroot, variables);

			}

			final GuiEventSignature signature = new GuiEventSignature(stageId, null, null, null, GuiEventType.STAGE_CREATED);
			final Map<String, Object> eventData = new HashMap<>();
			final GuiEvent event = new GuiEvent(null, signature, eventData);
			this.portGui().out(IGuiNotification.class).notifyEventOccured(event);

		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public <T extends IGuiScene> T createScene(final StageIdentity stageId, final SceneIdentity sceneId, final Class<T> sceneClass, final URL content) {
		final T sceneObj = this.createGuiScene(sceneClass, this.afId() + "." + sceneId.asPrimitive(), stageId, sceneId);
		return sceneObj;
	}

	<T extends IGuiScene> T createGuiScene(final Class<T> sceneClass, final String afId, final StageIdentity stageId, final SceneIdentity sceneId) {
		try {
			final ClassLoader loader = this.getClass().getClassLoader();
			final Class<?>[] interfaces = new Class<?>[] { sceneClass };
			final InvocationHandler h = this.af.createObject(VertxGuiSceneProxy.class, afId, this, stageId, sceneId);
			final Object proxy = Proxy.newProxyInstance(loader, interfaces, h);
			return (T) proxy;
		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, ex.getMessage(), ex);
		}
		return null;
	}

	@Override
	public void navigateTo(final UserSession session, final String location) {
		final JsonObject data = new JsonObject();
		data.put("location", location);
		// data.put("sceneArguments", new JsonObject(new HashMap<String, Object>(sceneArguments)));
		this.verticle.getComms().send(session, "Gui.navigateTo", data);
	}

	@Override
	public void download(final UserSession session, final String location, final String filename) {
		final String link = location.replace("${rootPath}", this.rootPath);
		final JsonObject data = new JsonObject();
		data.put("filename", filename);
		data.put("link", link);
		this.verticle.getComms().send(session, "Gui.download", data);
	}

	@Override
	public void upload(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String uploadLink,
			final String filenameElementId, final OnEventHandler handler) {
		final String uploadLink1 = uploadLink.replace("${rootPath}", this.rootPath);
		final JsonObject data = new JsonObject();
		data.put("filenameElementId", filenameElementId);
		data.put("uploadLink", uploadLink1);
		this.verticle.getComms().send(session, "Gui.upload", data);
		this.verticle.register("upload", (us, id, dt) -> {
			final GuiEventSignature signature = new GuiEventSignature(stageId, sceneId, null, null, GuiEventType.UPLOAD);
			final GuiEvent event = new GuiEvent(us, signature, dt);
			handler.execute(event);
		});
	}

	@Override
	public void switchTo(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final Map<String, String> sceneArguments) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("sceneArguments", new JsonObject(new HashMap<String, Object>(sceneArguments)));
		this.verticle.getComms().send(session, "Gui.switchToScene", data);
	}

	@Override
	public void newWindow(final UserSession session, final String location) {
		final JsonObject data = new JsonObject();
		data.put("location", location);
		this.verticle.getComms().send(session, "Gui.newWindow", data);
	}

	@Override
	public void dialogCreate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final DialogIdentity dialogId,
			final String content) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("dialogId", dialogId.asPrimitive());
		data.put("content", content);
		this.verticle.getComms().send(session, "Dialog.create", data);
	}

	@Override
	public void dialogOpen(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final DialogIdentity dialogId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("dialogId", dialogId.asPrimitive());
		this.verticle.getComms().send(session, "Dialog.open", data);
	}

	@Override
	public void dialogClose(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final DialogIdentity dialogId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("dialogId", dialogId.asPrimitive());
		this.verticle.getComms().send(session, "Dialog.close", data);
	}

	@Override
	public void setTitle(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("value", value);
		this.verticle.getComms().send(session, "Gui.setTitle", data);
	}

	@Override
	public void addElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String newElementId, final String type) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("newElementId", newElementId);
		data.put("type", type);

		this.verticle.getComms().send(session, "Element.add", data);

	};

	@Override
	public void addElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String newElementId, final String type, final String attributes, final Object content) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("newElementId", newElementId);
		data.put("type", type);
		data.put("content", content);

		final org.hjson.JsonValue hjson = org.hjson.JsonValue.readHjson(attributes.replaceAll("'", "\""));

		final String jsonStr = hjson.toString();// attributes.replaceAll("'", "\"");
		final JsonObject atts = new JsonObject(jsonStr);
		data.put("attributes", atts);

		this.verticle.getComms().send(session, "Element.add", data);
	}

	@Override
	public void removeElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);

		this.verticle.getComms().send(session, "Element.remove", data);
	}

	@Override
	public void elementClear(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);

		this.verticle.getComms().send(session, "Element.clear", data);
	}

	@Override
	public void elementSetDisabled(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final boolean value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);
		data.put("value", value);

		this.verticle.getComms().send(session, "Element.setDisabled", data);
	}

	@Override
	public void elementSetLoading(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final boolean value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);
		data.put("value", value);

		this.verticle.getComms().send(session, "Element.setLoading", data);
	}

	@Override
	public void elementSetProperty(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final String propertyName, final Object value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);
		data.put("property", propertyName);
		data.put("value", value);

		this.verticle.getComms().send(session, "Element.setProperty", data);
	}

	@Override
	public void elementAddClass(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final String className) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);
		data.put("className", className);
		this.verticle.getComms().send(session, "Element.addClass", data);
	}

	@Override
	public void elementRemoveClass(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final String className) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);
		data.put("className", className);
		this.verticle.getComms().send(session, "Element.removeClass", data);
	}

	@Override
	public void textSetValue(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String id, final String value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", id);
		data.put("value", value);

		this.verticle.getComms().send(session, "Gui.setText", data);
	}

	@Override
	public void tableCreate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);
		this.verticle.getComms().send(session, "Table.create", data);
	}

	@Override
	public void tableRemove(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);
		this.verticle.getComms().send(session, "Table.remove", data);
	}

	@Override
	public void tableAddColumn(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId,
			final String colHeaderContent, final String rowTemplateCellContent, final String existingRowCellContent) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);
		data.put("colHeaderContent", colHeaderContent);
		data.put("rowTemplateCellContent", rowTemplateCellContent);
		data.put("existingRowCellContent", existingRowCellContent);

		this.verticle.getComms().send(session, "Table.addColumn", data);
	}

	@Override
	public void tableClearAllColumnHeaders(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);

		this.verticle.getComms().send(session, "Table.clearAllColumnHeaders", data);
	}

	@Override
	public void tableAppendRow(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId,
			final Map<String, Object> rowData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);
		data.put("rowData", new JsonObject(rowData));

		this.verticle.getComms().send(session, "Table.appendRow", data);
	}

	@Override
	public void tableRemoveRow(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId, final String rowId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);
		data.put("rowId", rowId);

		this.verticle.getComms().send(session, "Table.removeRow", data);
	}

	@Override
	public void tableClearAllRows(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);

		this.verticle.getComms().send(session, "Table.clearAllRows", data);
	}

	@Override
	public void chartCreate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId, final String chartId,
			final String chartType, final String jsonChartData, final String jsonChartOptions) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("chartId", chartId);
		data.put("chartType", chartType);

		if (jsonChartData.startsWith("[")) {
			data.put("chartData", new JsonArray(jsonChartData));
		} else {
			data.put("chartData", new JsonObject(jsonChartData));
		}
		data.put("chartOptions", new JsonObject(jsonChartOptions));

		this.verticle.getComms().send(session, "Chart.create", data);
	}

	@Override
	public void chartRemove(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);

		this.verticle.getComms().send(session, "Chart.remove", data);
	}

	@Override
	public <X, Y> void chartAddDataItem(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String chartId,
			final String seriesName, final X x, final Y y) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("chartId", chartId);
		data.put("series", seriesName);
		data.put("x", x);
		data.put("y", y);

	}

	@Override
	public void diagramCreate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String jsonDiagramData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		final JsonObject ddata = new JsonObject(jsonDiagramData);
		data.put("data", ddata);

		this.verticle.getComms().send(session, "Diagram.create", data);
	}

	@Override
	public void diagramUpdate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String jsonDiagramData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		final JsonObject ddata = new JsonObject(jsonDiagramData);
		data.put("data", ddata);

		this.verticle.getComms().send(session, "Diagram.update", data);
	}

	@Override
	public void diagramRemove(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);

		this.verticle.getComms().send(session, "Diagram.remove", data);
	}

	@Override
	public <T extends IGuiDialog> T dialogCreate(final Class<T> dialogClass, final UserSession session, final IGuiScene scene, final DialogIdentity dialogId,
			final String title, final String dialogContent) {
		try {
			final ClassLoader loader = this.getClass().getClassLoader();
			final Class<?>[] interfaces = new Class<?>[] { dialogClass };
			final InvocationHandler h = this.af.createObject(VertxGuiDialogProxy.class, dialogId.asPrimitive(), this, scene, dialogId, dialogContent);
			final Object proxy = Proxy.newProxyInstance(loader, interfaces, h);

			return (T) proxy;
		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, ex.getMessage(), ex);
			return null;
		}
	}

	@Override
	public void editorCreate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String initialContent, final String languageId, final String optionsHJsonStr) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("initialContent", initialContent);

		// final JsonObject jsonLanguageDefinition = new JsonObject();
		// jsonLanguageDefinition.put("identity", languageId);
		// jsonLanguageDefinition.put("tokenHighlightingPatterns", new JsonArray(languageDefinition.getSyntaxHighlighting().stream().map((el) -> {
		// final JsonObject d = new JsonObject();
		// d.put("match", el.getPattern());
		// d.put("name", el.getLable());
		// return d;
		// }).collect(Collectors.toList())));

		data.put("languageId", languageId);

		final org.hjson.JsonValue hjson = org.hjson.JsonValue.readHjson(optionsHJsonStr.replaceAll("'", "\""));
		final JsonObject opts = new JsonObject(hjson.toString());
		data.put("options", opts);
		this.verticle.getComms().send(session, "Editor.create", data);
	}

	@Override
	public void editorDefineTextColourTheme(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String themeName,
			final Map<String, Tuple3<String, String, String>> theme) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("name", themeName);

		// TODO: this is maybe to specific to the monaco editor!
		final JsonArray jsonThemeRules = new JsonArray();
		for (final Map.Entry<String, Tuple3<String, String, String>> rule : theme.entrySet()) {
			final JsonObject jsonRule = new JsonObject();
			jsonRule.put("token", rule.getKey());
			jsonRule.put("foreground", rule.getValue().v1());
			jsonRule.put("fontStyle", rule.getValue().v2());
			jsonRule.put("background", rule.getValue().v3());
			jsonThemeRules.add(jsonRule);
		}

		data.put("rules", jsonThemeRules);

		this.verticle.getComms().send(session, "Editor.defineTextColourTheme", data);
	}

	@Override
	public void editorUpdateParseTree(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String editorId,
			final String jsonParseTreeData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("editorId", editorId);
		data.put("parseTree", new JsonObject(jsonParseTreeData));

		this.verticle.getComms().send(session, "Editor.updateParseTree", data);
	}

	@Override
	public void graphCreate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String jsonGraphData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		final JsonObject ddata = new JsonObject(jsonGraphData);
		data.put("data", ddata);

		this.verticle.getComms().send(session, "Graph.create", data);
	}

	@Override
	public void graphUpdate(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String jsonGraphData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		final JsonObject ddata = new JsonObject(jsonGraphData);
		data.put("data", ddata);

		this.verticle.getComms().send(session, "Graph.update", data);
	}

	@Override
	public void graphRemove(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);

		this.verticle.getComms().send(session, "Graph.remove", data);
	}

	// --------- Ports ---------
	@PortInstance
	@PortContract(provides = IGuiRequest.class, requires = IGuiNotification.class)
	@PortContract(provides = IAuthenticatorRequest.class, requires = IAuthenticatorNotification.class)
	IPort portGui;

	public IPort portGui() {
		return this.portGui;
	}
}
