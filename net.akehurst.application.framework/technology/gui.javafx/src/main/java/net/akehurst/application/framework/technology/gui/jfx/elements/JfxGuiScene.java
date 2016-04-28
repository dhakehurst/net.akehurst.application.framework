package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.elements.IChart;
import net.akehurst.application.framework.technology.guiInterface.elements.IText;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class JfxGuiScene implements IGuiScene, InvocationHandler {

	public JfxGuiScene(String id) {
		this.afId = id;
	}

	Parent root;

	public void setRoot(Parent value) {
		this.root = value;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().startsWith("get")) {
			Class<?> returnType = method.getReturnType();
			String name = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);

			if (IChart.class == returnType) {
				Node n = this.root.lookup("#" + name);
				if (n instanceof Chart) {
					Chart jfx = (Chart) n;
					return new JfxChart(jfx);
				} else {
					//TODO: element not found exception
					return null;
				}
			} if (IText.class == returnType) {
				Node n = this.root.lookup("#" + name);
				BetterMethodFinder bmf = new BetterMethodFinder(n.getClass());
				Method mGet = bmf.findMethod("getText");
				Method mSet = bmf.findMethod("setText", String.class);
				if (null!=mGet && null!=mSet) {
					InvocationHandler h = new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							try {
								if (method.getName().equals("getText")) {
									return mGet.invoke(n);
								} else if (method.getName().equals("setText")) {
									mSet.invoke(n, args[1]);
								}
							} catch (Throwable t) {
								t.printStackTrace();
							}
							return null;
						}
					};
					return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { IText.class }, h);

				} else {
					//not a text element
					return null;
				}
			} else {
				Node n = this.root.lookup("#" + name);
				if (n==null) {
					//not found
					return null;
				} else {
					return new JfxGuiElement(n);
				}
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
