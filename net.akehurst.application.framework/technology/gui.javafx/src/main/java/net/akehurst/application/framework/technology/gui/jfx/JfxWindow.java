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
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.os.AbstractComponent;
import net.akehurst.application.framework.technology.authentication.TechSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiNotification;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;

public class JfxWindow extends AbstractComponent implements IGuiRequest {

	
	public JfxWindow(String objectId) {
		super(objectId);
	}
	
	@ConfiguredValue(defaultValue="")
	String fxmlUrlStr;
	
	URL getFxmlUrl() {
		if (this.fxmlUrlStr.isEmpty()) {
			return null;
		} else {
			try {
				return new URL(this.fxmlUrlStr);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	Stage primary;
//	Canvas canvas;
//	GraphicsContext gc;
	
	@Override
	public void afConnectParts() {
	}
	
	@Override
	public void afRun() {
		new JFXPanel();
		Platform.runLater(new Runnable() {
			public void run() {
				try {
					primary = new Stage();
					primary.setTitle("Jfx Gui");

					URL url = getFxmlUrl();
					Parent root = null;
					if (null==url) {
						root = new Group();
					} else {
						root = FXMLLoader.load(url);
					}
					Scene scene = new Scene(root);

					primary.setScene(scene);
					primary.sizeToScene();
					primary.show();
					portGui().out(IGuiNotification.class).notifyReady();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void createStage(String stageId, boolean authenticated, URL content) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void createScene(String stageId, String sceneId, URL content) {
		// TODO Auto-generated method stub
		
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
		Node n = this.primary.getScene().lookup("#"+id);
		if (n instanceof TextInputControl) {
			((TextInputControl)n).setText(text);
		} else if (n instanceof Text) {
			((Text)n).setText(text);
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
	
	//--------- IGuiNotification ---------




	//--------- Ports ---------
	@PortInstance(provides={IGuiRequest.class},requires={IGuiNotification.class})
	IPort portGui;
	public IPort portGui() {
		return this.portGui;
	}
}
