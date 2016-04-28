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
package net.akehurst.application.framework.technology.gui.jfx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.UserDetails;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.realisation.ApplicationFramework;
import net.akehurst.application.framework.technology.gui.jfx.elements.JfxGuiScene;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;
import net.akehurst.application.framework.technology.guiInterface.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class JfxWindow extends AbstractComponent implements IGuiRequest {

	public JfxWindow(String objectId) {
		super(objectId);
		this.stages = new HashMap<>();
	}

	@ServiceReference
	IApplicationFramework af;

	Map<StageIdentity,Stage> stages;

	@Override
	public void afConnectParts() {
	}

	@Override
	public void afRun() {
		// ensure the Jfx library is initialised
		new JFXPanel();

		// This gui component is ready and started.
		portGui().out(IGuiNotification.class).notifyReady();
	}

	@Override
	public void createStage(StageIdentity stageId, boolean authenticated, URL content) {
		Platform.runLater(() -> {
			Stage primary = new Stage();
			primary.setTitle(stageId.asPrimitive());
			this.stages.put(stageId, primary);
			
			UserSession session = new UserSession("desktopSession", new UserDetails(System.getProperty("user.name")));
			GuiEventSignature signature = new GuiEventSignature(stageId, null, null, IGuiNotification.EVENT_STAGE_CREATED);
			Map<String, Object> eventData = new HashMap<>();
			GuiEvent event = new GuiEvent(session, signature, eventData);
			portGui().out(IGuiNotification.class).notifyEventOccured(event);
		});
	}

	@Override
	public <T extends IGuiScene> T createScene(StageIdentity stageId, SceneIdentity sceneId, Class<T> sceneClass, URL content) {
		// try {
		// T sceneObj = af.createObject(sceneClass, afId() + "." + sceneId);
		T sceneObj = createGuiScene(sceneClass, afId() + "." + sceneId.asPrimitive());
		Parent root = null;
		try {
			
			if (null == content) {
				root = new Group();
			} else {

				FXMLLoader fxmlLoader = new FXMLLoader(content);
				// fxmlLoader.setRoot(sceneObj);
				fxmlLoader.setController(sceneObj);
				fxmlLoader.load();
				root = fxmlLoader.getRoot();
			}
		} catch (Throwable t) {
			logger.log(LogLevel.ERROR, "Failed to create Scene " + sceneId.asPrimitive(), t);
		}
		((JfxGuiScene)Proxy.getInvocationHandler(sceneObj)).setRoot(root);
		
		
		Parent finalRoot = root;
		Platform.runLater(() -> {
			try {
				Scene scene = new Scene(finalRoot);
				Stage primary = this.stages.get(stageId);
				primary.setScene(scene);
				primary.sizeToScene();

				primary.addEventHandler(WindowEvent.WINDOW_SHOWN, (ev) -> {
					UserSession session = new UserSession("desktopSession", new UserDetails(System.getProperty("user.name")));
					GuiEventSignature signature = new GuiEventSignature(stageId, sceneId, null, IGuiNotification.EVENT_SCENE_LOADED);
					Map<String, Object> eventData = new HashMap<>();
					GuiEvent event = new GuiEvent(session, signature, eventData);
					portGui().out(IGuiNotification.class).notifyEventOccured(event);
				});

				primary.show();

			} catch (Throwable t) {
				logger.log(LogLevel.ERROR, "Failed to create Scene " + sceneId.asPrimitive(), t);
			}
		});
		return sceneObj;
	}

	<T extends IGuiScene> T createGuiScene(Class<T> sceneClass, String afId) {
		ClassLoader loader = this.getClass().getClassLoader();
		Class<?>[] interfaces = new Class<?>[] { sceneClass };
		InvocationHandler h = new JfxGuiScene(afId);
		Object proxy = Proxy.newProxyInstance(loader, interfaces, h);
		return (T) proxy;
	}

	@Override
	public void requestRecieveEvent(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId, String eventType) {
		// Platform.runLater(() -> {
		// Node n = this.primary.getScene().lookup("#" + elementId);
		// EventType<Event> eventType;
		// n.addEventHandler(eventType, (ev)->{
		// GuiEventSignature signature = new GuiEventSignature(stageId, null, null, IGuiNotification.EVENT_SCENE_LOADED);
		// Map<String, Object> eventData = new HashMap<>();
		// GuiEvent event = new GuiEvent(session, signature, eventData);
		// portGui().out(IGuiNotification.class).notifyEventOccured(event);
		// });
		// });

	}

	@Override
	public void addElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String newElementId, String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String newElementId, String type, String attributes, Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void switchTo(UserSession session, StageIdentity stageId, SceneIdentity sceneId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setText(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String id, String text) {
		Stage primary = this.stages.get(stageId);
		Node n = primary.getScene().lookup("#" + id);
		if (n instanceof TextInputControl) {
			((TextInputControl) n).setText(text);
		} else if (n instanceof Text) {
			((Text) n).setText(text);
		}
	}

	@Override
	public void setTitle(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChart(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String chartId, Integer width, Integer height, String chartType,
			String jsonChartData, String jsonChartOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDiagram(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String diagramId, String jsonDiagramData) {
		// TODO Auto-generated method stub

	}

	// --------- IGuiNotification ---------

	// --------- Ports ---------
	@PortInstance(provides = { IGuiRequest.class }, requires = { IGuiNotification.class })
	IPort portGui;

	public IPort portGui() {
		return this.portGui;
	}
}
