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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import net.akehurst.application.framework.components.AbstractComponent;
import net.akehurst.application.framework.components.Port;
import net.akehurst.application.framework.os.annotations.ConfiguredValue;
import net.akehurst.application.framework.os.annotations.PortInstance;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorRequest;
import net.akehurst.application.framework.technology.authentication.TechSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;

public class VertxWebsite extends AbstractComponent implements IGuiRequest, IAuthenticatorRequest {

	public VertxWebsite(String objectId) {
		super(objectId);
	}

	@ConfiguredValue(defaultValue="9999")
	IpPort port;
	
	AVerticle verticle;

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
	public void requestLogin(TechSession session, String username, String password) {
		this.verticle.requestLogin(session, username, password);
	}

	@Override
	public void requestLogout(TechSession session) {
		this.verticle.requestLogout(session);
	}

	
	// --------- IGuiRequest ---------
	@Override
	public void requestRecieveEvent(TechSession session, String sceneId, String elementId, String eventType) {
		JsonObject data = new JsonObject();
		data.put("elementId", elementId);
		data.put("eventType", eventType);
		
		this.verticle.comms.send(session, "Gui.requestRecieveEvent", data);
		
	}

	@Override
	public void createStage(String stageId, boolean authenticated, URL contentRoot) {
		try {
			String str = contentRoot.toString();
			if (str.endsWith("/")) {
				str = str.substring(0, str.length()-1);
			}
			String webroot =  str.substring(str.lastIndexOf('/')+1);
			String routePath = stageId+"/*";
			if (authenticated) {
				this.verticle.addAuthenticatedRoute(routePath, (rc -> {
					this.verticle.comms.activeSessions.put(rc.session().id(), rc.session());
					User u = rc.user();
					String path = rc.normalisedPath();
					System.out.println(path+" "+(null==u?"null":u.principal()));
					rc.next();
				}), webroot);
			} else {
				this.verticle.addRoute(routePath, (rc -> {
					this.verticle.comms.activeSessions.put(rc.session().id(), rc.session());
					User u = rc.user();
					String path = rc.normalisedPath();
					System.out.println(path+" "+(null==u?"null":u.principal()));
					rc.next();
				}), webroot);
			}
			this.verticle.comms.addSocksChannel("/sockjs"+routePath, (session, channelId, data) -> {
				if ("IGuiNotification.notifyEventOccured".equals(channelId)) {
					String sceneId = data.getString("sceneId");
					String eventType = data.getString("eventType");
					String elementId = data.getString("elementId");
					Map<String, Object> eventData = (Map<String, Object>) data.getJsonObject("eventData").getMap();
					this.portGui().out(IGuiNotification.class).notifyEventOccured(session, sceneId, elementId, eventType, eventData);
				} else  {
					//??
				}
			});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void createScene(String stageId, String sceneId, URL content) {
		// TODO Auto-generated method stub
		
	}
	
	public void createScene(String sceneId, boolean authenticated, URL contentRoot) {

	}

	@Override
	public void switchTo(TechSession session, String stageId, String sceneId) {
		JsonObject data = new JsonObject();
		data.put("stageId", stageId);
		data.put("sceneId", sceneId);
		
		this.verticle.comms.send(session, "Gui.switchToScene", data);
	}

	@Override
	public void setTitle(TechSession session, String sceneId, String value) {
		JsonObject data = new JsonObject();
		data.put("value", value);
		this.verticle.comms.send(session, "Gui.setTitle", data);
	}

	@Override
	public void addElement(TechSession session, String sceneId, String parentId, String newElementId, String type) {
		 JsonObject data = new JsonObject();
		 data.put("parentId", parentId);
		 data.put("newElementId", newElementId);
		 data.put("type", type);

		 this.verticle.comms.send(session, "Gui.addElement", data);

	};

	@Override
	public void addElement(TechSession session, String sceneId, String parentId, String newElementId, String type, String attributes, Object content) {
		 JsonObject data = new JsonObject();
		 data.put("parentId", parentId);
		 data.put("newElementId", newElementId);
		 data.put("type", type);
		 data.put("content", content);
		 String jsonStr = attributes.replaceAll("'", "\"");
		 JsonObject atts = new JsonObject(jsonStr);
		 data.put("attributes", atts);
		 
		 this.verticle.comms.send(session,"Gui.addElement", data);
	}

	@Override
	public void clearElement(TechSession session, String sceneId, String elementId) {
		 JsonObject data = new JsonObject();
		 data.put("elementId", elementId);
		 
		 this.verticle.comms.send(session,"Gui.clearElement", data);
	}
	
	@Override
	public void setText(TechSession session, String sceneId, String id, String value) {
		JsonObject data = new JsonObject();
		data.put("id", id);
		data.put("value", value);
		
		this.verticle.comms.send(session, "Gui.setText", data);
	}
	
	
	// --------- Ports ---------
	@PortInstance(provides={IGuiRequest.class,IAuthenticatorRequest.class},requires={IGuiNotification.class,IAuthenticatorNotification.class})
	Port portGui;

	public Port portGui() {
		return this.portGui;
	}
}
