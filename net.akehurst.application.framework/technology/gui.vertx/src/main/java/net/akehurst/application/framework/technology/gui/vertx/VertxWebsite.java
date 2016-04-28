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
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorRequest;
import net.akehurst.application.framework.technology.gui.vertx.elements.VertxGuiScene;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;
import net.akehurst.application.framework.technology.guiInterface.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;

public class VertxWebsite extends AbstractComponent implements IGuiRequest, IAuthenticatorRequest {

	public VertxWebsite(String objectId) {
		super(objectId);
	}

	@ServiceReference
	ILogger logger;
	
	@ConfiguredValue(defaultValue = "")
	String rootPath;

	@ConfiguredValue(defaultValue = "/test")
	String testPath;

	String getTestPath() {
		return this.rootPath + this.testPath;
	}

	@ConfiguredValue(defaultValue = "/download")
	String downloadPath;

	String getDownloadPath() {
		return this.rootPath + this.downloadPath;
	}

	@ConfiguredValue(defaultValue = "/upload")
	String uploadPath;

	String getUploadPath() {
		return this.rootPath + this.uploadPath;
	}

	@ConfiguredValue(defaultValue = "/js")
	String jsPath;

	String getJsPath() {
		return this.rootPath + this.jsPath;
	}

	String getSockjsPath() {
		return "/sockjs"; // this value is hard coded in index-script.js, they must match
	}

	@ConfiguredValue(defaultValue = "9999")
	IpPort port;

	AVerticle verticle;
	
	Map<SceneIdentity, IGuiScene> scenes;
	

	@Override
	public void afConnectParts() {
	}

	@Override
	public void afRun() {
		this.verticle = new AVerticle(this, port.asPrimitive());
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(this.verticle);
	}

	// --------- IAuthenticatorRequest ---------
	@Override
	public void requestLogin(UserSession session, String username, String password) {
		this.verticle.requestLogin(session, username, password);
	}

	@Override
	public void requestLogout(UserSession session) {
		this.verticle.requestLogout(session);
	}

	// --------- IGuiRequest ---------
	@Override
	public void requestRecieveEvent(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId, String eventType) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("elementId", elementId);
		data.put("eventType", eventType);

		this.verticle.comms.send(session, "Gui.requestRecieveEvent", data);

	}

	@Override
	public void createStage(StageIdentity stageId, boolean authenticated, URL contentRoot) {
		try {
			Map<String, String> variables = new HashMap<>();
			variables.put("rootPath", this.rootPath);
			variables.put("jsPath", this.getJsPath());

			String str = contentRoot.toString();
			if (str.endsWith("/")) {
				str = str.substring(0, str.length() - 1);
			}
			String webroot = str.substring(str.lastIndexOf('/') + 1);
			String routePath = this.rootPath + stageId.asPrimitive();
			if (authenticated) {
				this.verticle.addAuthenticatedRoute(routePath, (rc -> {
					this.verticle.comms.activeSessions.put(rc.session().id(), rc.session());
					User u = rc.user();
					String path = rc.normalisedPath();
					System.out.println(path + " " + (null == u ? "null" : u.principal()));
					rc.next();
				}), webroot, variables);
			} else {
				this.verticle.addRoute(routePath, (rc -> {
					this.verticle.comms.activeSessions.put(rc.session().id(), rc.session());
					User u = rc.user();
					String path = rc.normalisedPath();
					System.out.println(path + " " + (null == u ? "null" : u.principal()));
					rc.next();
				}), webroot, variables);
			}
			
			GuiEventSignature signature = new GuiEventSignature(stageId, null, null, IGuiNotification.EVENT_STAGE_CREATED);
			Map<String, Object> eventData = new HashMap<>();
			GuiEvent event = new GuiEvent(null, signature, eventData );
			portGui().out(IGuiNotification.class).notifyEventOccured(event );
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public <T extends IGuiScene> T createScene(StageIdentity stageId, SceneIdentity sceneId, Class<T> sceneClass, URL content) {
		T sceneObj = createGuiScene(sceneClass, afId() + "." + sceneId.asPrimitive(), stageId, sceneId);

		((VertxGuiScene)Proxy.getInvocationHandler(sceneObj)).setGuiRequest(this);
		
		return sceneObj;
	}
	
	<T extends IGuiScene> T createGuiScene(Class<T> sceneClass, String afId,StageIdentity stageId, SceneIdentity sceneId) {
		ClassLoader loader = this.getClass().getClassLoader();
		Class<?>[] interfaces = new Class<?>[] { sceneClass };
		InvocationHandler h = new VertxGuiScene(afId, stageId, sceneId);
		Object proxy = Proxy.newProxyInstance(loader, interfaces, h);
		return (T) proxy;
	}

	@Override
	public void switchTo(UserSession session, StageIdentity stageId, SceneIdentity sceneId) {
		JsonObject data = new JsonObject();
		data.put("stageId", this.rootPath + stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());

		this.verticle.comms.send(session, "Gui.switchToScene", data);
	}

	@Override
	public void setTitle(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String value) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("value", value);
		this.verticle.comms.send(session, "Gui.setTitle", data);
	}

	@Override
	public void addElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String newElementId, String type) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("newElementId", newElementId);
		data.put("type", type);

		this.verticle.comms.send(session, "Gui.addElement", data);

	};

	@Override
	public void addElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String newElementId, String type, String attributes, Object content) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("newElementId", newElementId);
		data.put("type", type);
		data.put("content", content);
		String jsonStr = attributes.replaceAll("'", "\"");
		JsonObject atts = new JsonObject(jsonStr);
		data.put("attributes", atts);

		this.verticle.comms.send(session, "Gui.addElement", data);
	}

	@Override
	public void clearElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("elementId", elementId);

		this.verticle.comms.send(session, "Gui.clearElement", data);
	}

	@Override
	public void setText(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String id, String value) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("id", id);
		data.put("value", value);

		this.verticle.comms.send(session, "Gui.setText", data);
	}

	@Override
	public void addChart(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String chartId, Integer width, Integer height, String chartType,
			String jsonChartData, String jsonChartOptions) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("chartId", chartId);
		data.put("width", width);
		data.put("height", height);
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
	public void addDiagram(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String diagramId, String jsonDiagramData) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId.asPrimitive());
		data.put("sceneId", sceneId.asPrimitive());
		data.put("parentId", parentId);
		data.put("diagramId", diagramId);
		JsonObject ddata = new JsonObject(jsonDiagramData);
		data.put("data", ddata);

		this.verticle.comms.send(session, "Gui.addDiagram", data);
	}

	// TODO: deal with buttons
	public void createModal(UserSession ts, StageIdentity stageId, SceneIdentity sceneId, String parentId, String modalId, String title, String modalContent) {
		String content = "";
		content += "<div id='" + modalId + "' class='modal fade' role='dialog'>";
		content += "  <div class='modal-dialog'>";
		content += "    <fieldset class='modal-content'>";
		content += "      <div class='modal-header'>";
		content += "        <button type='button' class='close' data-dismiss='modal'>&times;</button>";
		content += "        <h4 class='modal-title'>" + title + "</h4>";
		content += "      </div>";
		content += "      <div class='modal-body'>";
		content += modalContent;
		content += "      </div>";
		content += "      <div class='modal-footer'>";
		content += "        <button type='button' class='btn btn-default' data-dismiss='modal'>Close</button>";
		content += "      </div>";
		content += "    </fieldset>";
		content += "  </div>";
		this.addElement(ts, stageId, sceneId, parentId, modalId, "div", "{'class':'modal fade','role':'dialog'}", content);
	}

	// --------- Ports ---------
	@PortInstance(provides = { IGuiRequest.class, IAuthenticatorRequest.class }, requires = { IGuiNotification.class, IAuthenticatorNotification.class })
	IPort portGui;

	public IPort portGui() {
		return this.portGui;
	}
}
