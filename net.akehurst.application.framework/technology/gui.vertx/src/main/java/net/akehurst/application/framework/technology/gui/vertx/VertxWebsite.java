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
import net.akehurst.application.framework.technology.interfaceGui.GuiException;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class VertxWebsite extends AbstractComponent implements IGuiRequest {

	public VertxWebsite(final String objectId) {
		super(objectId);
	}

	@ServiceReference
	IApplicationFramework af;

	@ServiceReference
	ILogger logger;

	@ConfiguredValue(defaultValue = "")
	String rootPath;

	String getNormalisedRootPath() {
		return 0 == this.rootPath.length() ? "/" : "/" + this.rootPath + "/";
	}

	@ConfiguredValue(defaultValue = "test/")
	String testPath;

	String getTestPath() {
		return this.getNormalisedRootPath() + this.testPath;
	}

	@ConfiguredValue(defaultValue = "download/")
	String downloadPath;

	String getDownloadPath() {
		return this.getNormalisedRootPath() + this.downloadPath;
	}

	@ConfiguredValue(defaultValue = "upload/")
	String uploadPath;

	String getUploadPath() {
		return this.getNormalisedRootPath() + this.uploadPath;
	}

	@ConfiguredValue(defaultValue = "js")
	String jsPath;

	String getJsPath() {
		return this.getNormalisedRootPath() + this.jsPath;
	}

	String getSockjsPath() {
		return "sockjs/"; // this value is hard coded in index-script.js, they must match
	}

	@CommandLineArgument(description = "Override the default (9999) port")
	@ConfiguredValue(defaultValue = "9999")
	IpPort port;

	AVerticle verticle;

	Map<SceneIdentity, IGuiScene> scenes;

	@Override
	public void afConnectParts() {}

	@Override
	public void afRun() {
		this.verticle = new AVerticle(this, this.port.asPrimitive());
		final Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(this.verticle);
	}

	// --------- IGuiRequest ---------
	@Override
	public void authenticate(final UserSession session) throws GuiException {
		this.verticle.authenticate(session);
	}

	@Override
	public void requestRecieveEvent(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final String eventType) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("elementId", elementId);
		data.put("eventType", eventType);

		this.verticle.comms.send(session, "Gui.requestRecieveEvent", data);

	}

	@Override
	public void createStage(final StageIdentity stageId, final boolean authenticated, final URL contentRoot) {
		try {
			final Map<String, String> variables = new HashMap<>();
			variables.put("rootPath", this.rootPath);
			final String js = this.getJsPath().startsWith("/") ? this.getJsPath().substring(1) : this.getJsPath();
			variables.put("jsPath", js);
			variables.put("stageId", stageId.asPrimitive());

			final String stagePath = stageId.asPrimitive().isEmpty() ? "" : stageId.asPrimitive() + "/";

			String str = contentRoot.toString();
			if (str.endsWith("/")) {
				str = str.substring(0, str.length() - 1);
			}
			final String webroot = str.substring(str.lastIndexOf('/') + 1);
			final String routePath = this.getNormalisedRootPath() + stagePath;
			if (authenticated) {
				this.verticle.addAuthenticatedRoute(routePath, rc -> {
					this.verticle.comms.activeSessions.put(rc.session().id(), rc.session());
					final User u = rc.user();
					final String path = rc.normalisedPath();
					System.out.println(path + " " + (null == u ? "null" : u.principal()));
					rc.next();
				}, webroot, variables);
			} else {
				this.verticle.addRoute(routePath, rc -> {
					this.verticle.comms.activeSessions.put(rc.session().id(), rc.session());
					final User u = rc.user();
					final String path = rc.normalisedPath();
					System.out.println(path + " " + (null == u ? "null" : u.principal()));
					rc.next();
				}, webroot, variables);
			}

			final GuiEventSignature signature = new GuiEventSignature(stageId, null, null, null, IGuiNotification.EVENT_STAGE_CREATED);
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
	public void switchTo(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final Map<String, String> sceneArguments) {
		final JsonObject data = new JsonObject();
		data.put("stageId", this.rootPath + stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("sceneArguments", new JsonObject(new HashMap<String, Object>(sceneArguments)));
		this.verticle.comms.send(session, "Gui.switchToScene", data);
	}

	@Override
	public void showDialog(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final DialogIdentity dialogId,
			final String content) {
		final JsonObject data = new JsonObject();
		data.put("stageId", this.rootPath + stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", "dialogs");
		data.put("dialogId", dialogId.asPrimitive());
		data.put("content", content);
		this.verticle.comms.send(session, "Gui.showDialog", data);
	}

	@Override
	public void setTitle(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("value", value);
		this.verticle.comms.send(session, "Gui.setTitle", data);
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

		this.verticle.comms.send(session, "Gui.addElement", data);

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
		final String jsonStr = attributes.replaceAll("'", "\"");
		final JsonObject atts = new JsonObject(jsonStr);
		data.put("attributes", atts);

		this.verticle.comms.send(session, "Gui.addElement", data);
	}

	@Override
	public void removeElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("elementId", elementId);

		this.verticle.comms.send(session, "Gui.removeElement", data);
	}

	@Override
	public void elementClear(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("elementId", elementId);

		this.verticle.comms.send(session, "Gui.elementClear", data);
	}

	@Override
	public void elementDisable(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final boolean value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("elementId", elementId);
		data.put("value", value);

		this.verticle.comms.send(session, "Gui.elementDisable", data);
	}

	@Override
	public void set(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId, final String propertyName,
			final Object value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", elementId);
		data.put("property", propertyName);
		data.put("value", value);

		this.verticle.comms.send(session, "Gui.set", data);
	}

	@Override
	public void setText(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String id, final String value) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", id);
		data.put("value", value);

		this.verticle.comms.send(session, "Gui.setText", data);
	}

	@Override
	public void tableAppendRow(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId,
			final Map<String, Object> rowData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);
		data.put("rowData", new JsonObject(rowData));

		this.verticle.comms.send(session, "Table.appendRow", data);
	}

	@Override
	public void tableRemoveRow(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId, final String rowId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);
		data.put("rowId", rowId);

		this.verticle.comms.send(session, "Table.removeRow", data);
	}

	@Override
	public void tableClearAllRows(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("tableId", tableId);

		this.verticle.comms.send(session, "Table.clearAllRows", data);
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

		this.verticle.comms.send(session, "Gui.addChart", data);
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
	public void createDiagram(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String jsonDiagramData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		final JsonObject ddata = new JsonObject(jsonDiagramData);
		data.put("data", ddata);

		this.verticle.comms.send(session, "Gui.createDiagram", data);
	}

	@Override
	public void updateDiagram(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String jsonDiagramData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		final JsonObject ddata = new JsonObject(jsonDiagramData);
		data.put("data", ddata);

		this.verticle.comms.send(session, "Gui.updateDiagram", data);
	}

	@Override
	public <T extends IGuiDialog> T createDialog(final Class<T> dialogClass, final UserSession session, final IGuiScene scene, final DialogIdentity dialogId,
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
	public void addEditor(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String initialContent, final String languageId) {
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

		this.verticle.comms.send(session, "Editor.addEditor", data);
	}

	@Override
	public void updateParseTree(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String editorId,
			final String jsonParseTreeData) {
		final JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("editorId", editorId);
		data.put("parseTree", new JsonObject(jsonParseTreeData));

		this.verticle.comms.send(session, "Editor.updateParseTree", data);
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
