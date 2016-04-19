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

import java.net.MalformedURLException;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.authentication.TechSession;
import net.akehurst.application.framework.technology.authentication.TechUserDetails;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class JfxWindow extends AbstractComponent implements IGuiRequest {

	public JfxWindow(String objectId) {
		super(objectId);
	}

	Stage primary;
	// Canvas canvas;
	// GraphicsContext gc;

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
	public void createStage(String stageId, boolean authenticated, URL content) {
		Platform.runLater(() -> {
			primary = new Stage();
			primary.setTitle(stageId);

			TechSession session = new TechSession("desktopSession", new TechUserDetails(System.getProperty("user.name")));
			GuiEventSignature signature = new GuiEventSignature(stageId, null, null,  IGuiNotification.EVENT_STAGE_CREATED);
			Map<String, Object> eventData = new HashMap<>();
			GuiEvent event = new GuiEvent(session, signature, eventData);
			portGui().out(IGuiNotification.class).notifyEventOccured(event);
		});
	}

	@Override
	public void createScene(String stageId, String sceneId, URL content) {
		Platform.runLater(() -> {
			try {

				Parent root = null;
				if (null == content) {
					root = new Group();
				} else {
					root = FXMLLoader.load(content);
				}
				Scene scene = new Scene(root);

				primary.setScene(scene);
				primary.sizeToScene();

				primary.addEventHandler(WindowEvent.WINDOW_SHOWN, (ev)->{
					TechSession session = new TechSession("desktopSession", new TechUserDetails(System.getProperty("user.name")));
					GuiEventSignature signature = new GuiEventSignature(stageId, null, null, IGuiNotification.EVENT_SCENE_LOADED);
					Map<String, Object> eventData = new HashMap<>();
					GuiEvent event = new GuiEvent(session, signature, eventData);
					portGui().out(IGuiNotification.class).notifyEventOccured(event);	
				});

				
				primary.show();
			
			} catch (Throwable t) {
				logger.log(LogLevel.ERROR, "Failed to create Scene "+sceneId,t);
			}
		});
	}

	@Override
	public void requestRecieveEvent(TechSession session, String sceneId, String elementId, String eventType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addElement(TechSession session, String sceneId, String parentId, String newElementId, String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addElement(TechSession session, String sceneId, String parentId, String newElementId, String type, String attributes, Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearElement(TechSession session, String sceneId, String elementId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchTo(TechSession session, String stageId, String sceneId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setText(TechSession session, String sceneId, String id, String text) {
		Node n = this.primary.getScene().lookup("#" + id);
		if (n instanceof TextInputControl) {
			((TextInputControl) n).setText(text);
		} else if (n instanceof Text) {
			((Text) n).setText(text);
		}
	}

	@Override
	public void setTitle(TechSession session, String sceneId, String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChart(TechSession session, String sceneId, String parentId, String chartId, Integer width, Integer height, String chartType,
			String jsonChartData, String jsonChartOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDiagram(TechSession session, String sceneId, String parentId, String diagramId, String jsonDiagramData) {
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
