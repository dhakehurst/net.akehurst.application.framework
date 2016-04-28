package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;
import net.akehurst.application.framework.technology.guiInterface.StageIdentity;
import net.akehurst.application.framework.technology.guiInterface.elements.IChart;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiElement;
import net.akehurst.application.framework.technology.guiInterface.elements.ITable;
import net.akehurst.application.framework.technology.guiInterface.elements.IText;

public class VertxGuiScene implements IGuiScene, InvocationHandler {

	public VertxGuiScene(String afId, StageIdentity stageId, SceneIdentity sceneId) {
		this.afId = afId;
		this.stageId = stageId;
		this.sceneId = sceneId;
	}

	IGuiRequest guiRequest;
	StageIdentity stageId;
	SceneIdentity sceneId;

	public void setGuiRequest(IGuiRequest value) {
		this.guiRequest = value;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> returnType = method.getReturnType();
		if (method.getName().startsWith("get") || method.getName().startsWith("set")) {
			String elementName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);

			if (IGuiElement.class == returnType) {
				return new VertxGuiElement(this.guiRequest, this.stageId, this.sceneId, elementName);
			} else if (IText.class == returnType) {
				return new VertxGuiText(this.guiRequest, this.stageId, this.sceneId, elementName);
			} else if (IChart.class == returnType) {
				return null;
			} else if (ITable.class == returnType) {
				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	@Override
	public void notifyEventOccured(GuiEvent event) {
		// TODO Auto-generated method stub

	}


}
