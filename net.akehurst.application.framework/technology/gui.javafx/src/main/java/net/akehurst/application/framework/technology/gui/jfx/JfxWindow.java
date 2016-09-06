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
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.gui.jfx.elements.JfxGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class JfxWindow extends AbstractComponent implements IGuiRequest {

	public JfxWindow(final String objectId) {
		super(objectId);
		this.stages = new HashMap<>();
	}

	@ServiceReference
	IApplicationFramework af;

	Map<StageIdentity, Stage> stages;

	@Override
	public void afConnectParts() {}

	@Override
	public void afRun() {
		// ensure the Jfx library is initialised
		new JFXPanel();

		// This gui component is ready and started.
		this.portGui().out(IGuiNotification.class).notifyReady();
	}

	@Override
	public void createStage(final StageIdentity stageId, final boolean authenticated, final URL content) {
		Platform.runLater(() -> {
			final Stage primary = new Stage();
			primary.setTitle(stageId.asPrimitive());
			this.stages.put(stageId, primary);

			final UserSession session = new UserSession("desktopSession", new UserDetails(System.getProperty("user.name")));
			final GuiEventSignature signature = new GuiEventSignature(stageId, null, null, IGuiNotification.EVENT_STAGE_CREATED);
			final Map<String, Object> eventData = new HashMap<>();
			final GuiEvent event = new GuiEvent(session, signature, eventData);
			this.portGui().out(IGuiNotification.class).notifyEventOccured(event);
		});
	}

	@Override
	public <T extends IGuiScene> T createScene(final StageIdentity stageId, final SceneIdentity sceneId, final Class<T> sceneClass, final URL content) {
		// try {
		// T sceneObj = af.createObject(sceneClass, afId() + "." + sceneId);
		final T sceneObj = this.createGuiScene(sceneClass, this.afId() + "." + sceneId.asPrimitive());
		Parent root = null;
		try {

			if (null == content) {
				root = new Group();
			} else {

				final FXMLLoader fxmlLoader = new FXMLLoader(content);
				// fxmlLoader.setRoot(sceneObj);
				fxmlLoader.setController(sceneObj);
				fxmlLoader.load();
				root = fxmlLoader.getRoot();
			}
		} catch (final Throwable t) {
			this.logger.log(LogLevel.ERROR, "Failed to create Scene " + sceneId.asPrimitive(), t);
		}
		((JfxGuiScene) Proxy.getInvocationHandler(sceneObj)).setRoot(root);

		final Parent finalRoot = root;
		Platform.runLater(() -> {
			try {
				final Scene scene = new Scene(finalRoot);
				final Stage primary = this.stages.get(stageId);
				primary.setScene(scene);
				primary.sizeToScene();

				primary.addEventHandler(WindowEvent.WINDOW_SHOWN, (ev) -> {
					final UserSession session = new UserSession("desktopSession", new UserDetails(System.getProperty("user.name")));
					final GuiEventSignature signature = new GuiEventSignature(stageId, sceneId, null, IGuiNotification.EVENT_SCENE_LOADED);
					final Map<String, Object> eventData = new HashMap<>();
					final GuiEvent event = new GuiEvent(session, signature, eventData);
					this.portGui().out(IGuiNotification.class).notifyEventOccured(event);
				});

				primary.show();

			} catch (final Throwable t) {
				this.logger.log(LogLevel.ERROR, "Failed to create Scene " + sceneId.asPrimitive(), t);
			}
		});
		return sceneObj;
	}

	<T extends IGuiScene> T createGuiScene(final Class<T> sceneClass, final String afId) {
		final ClassLoader loader = this.getClass().getClassLoader();
		final Class<?>[] interfaces = new Class<?>[] { sceneClass };
		final InvocationHandler h = new JfxGuiScene(afId);
		final Object proxy = Proxy.newProxyInstance(loader, interfaces, h);
		return (T) proxy;
	}

	@Override
	public <T extends IGuiDialog> T createDialog(final Class<T> dialogClass, final UserSession session, final IGuiScene scene, final String modalId,
			final String title, final String dialogContent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showDialog(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String dialogId,
			final String dialogContent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestRecieveEvent(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId,
			final String eventType) {
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
	public void addElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String newElementId, final String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId,
			final String newElementId, final String type, final String attributes, final Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String dialogId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearElement(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchTo(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final Map<String, String> sceneArguments) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String elementName, final String propertyName,
			final Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setText(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String id, final String text) {
		final Stage primary = this.stages.get(stageId);
		final Node n = primary.getScene().lookup("#" + id);
		if (n instanceof TextInputControl) {
			((TextInputControl) n).setText(text);
		} else if (n instanceof Text) {
			((Text) n).setText(text);
		}
	}

	@Override
	public void setTitle(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tableAppendRow(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String tableId,
			final Map<String, Object> rowData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChart(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId, final String chartId,
			final Integer width, final Integer height, final String chartType, final String jsonChartData, final String jsonChartOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDiagram(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String parentId, final String diagramId,
			final String jsonDiagramData) {
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
