package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.SceneIdentity;
import net.akehurst.application.framework.technology.guiInterface.StageIdentity;
import net.akehurst.application.framework.technology.guiInterface.elements.IChart;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiMenuItem;
import net.akehurst.application.framework.technology.guiInterface.elements.IText;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class JfxGuiScene implements IGuiScene, InvocationHandler {

	public JfxGuiScene(String id) {
		this.afId = id;
	}

	Parent root;

	public void setRoot(Parent value) {
		this.root = value;
		
		value.addEventHandler(EventType.ROOT, (e)->{
			UserSession session = null;
			StageIdentity stageId= null;
			SceneIdentity sceneId = null;
			String elementId= null;
			String eventType= null;
			GuiEventSignature signature = new GuiEventSignature(stageId, sceneId, elementId, eventType);
			Map<String, Object> eventData = new HashMap<>();
			this.notifyEventOccured(new GuiEvent(session, signature, eventData));
		});
		
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
				return new JfxText(n);
//				BetterMethodFinder bmf = new BetterMethodFinder(n.getClass());
//				Method mGet = bmf.findMethod("getText");
//				Method mSet = bmf.findMethod("setText", String.class);
//				if (null!=mGet && null!=mSet) {
//					InvocationHandler h = new InvocationHandler() {
//						@Override
//						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//							try {
//								if (method.getName().equals("getText")) {
//									return mGet.invoke(n);
//								} else if (method.getName().equals("setText")) {
//									mSet.invoke(n, args[1]);
//								}
//							} catch (Throwable t) {
//								t.printStackTrace();
//							}
//							return null;
//						}
//					};
//					return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { IText.class }, h);


			} if (IGuiMenuItem.class == returnType) {
				//this.root.lookup("#" + name);
				//can't use lookup for menuitems
				MenuItem menuItem = this.lookupMenuItemInNodes(this.root.getChildrenUnmodifiable(), name);

				return new JfxMenuItem(menuItem);
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

	private MenuItem lookupMenuItemInNodes(List<Node> nodes, String id) {
		MenuItem result = null;
		for(Node n: nodes) {
			if (n instanceof MenuBar) {
				MenuBar mb = (MenuBar)n;
				for(Menu m: mb.getMenus()) {
					result = this.lookupMenuItem(m.getItems(), id);
				}
			} else if (n instanceof TreeView<?>) {
				TreeView<?> tv = (TreeView<?>)n;
				result = this.lookupMenuItem(tv.getContextMenu().getItems(), id);
			} else {
				//?
			}
			if (null==result) {
				if(n instanceof Parent) {
					Parent p = (Parent)n;
					result = this.lookupMenuItemInNodes(((Parent) n).getChildrenUnmodifiable(), id);
				}
			}
		}
		return result;
	}
	
	private MenuItem lookupMenuItem(List<MenuItem> items, String id) {
		
		for(MenuItem mi: items) {
			if(null!=mi.getId() && mi.getId().equals(id)) {
				return mi;
			}
			if (mi instanceof Menu) {
				MenuItem fmi = this.lookupMenuItem(((Menu)mi).getItems(), id);
				if (null!=fmi) {
					return fmi;
				}
			}
		}
		return null;
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
