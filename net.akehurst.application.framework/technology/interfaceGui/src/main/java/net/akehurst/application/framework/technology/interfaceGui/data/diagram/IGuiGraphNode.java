package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.Map;

public interface IGuiGraphNode {

	String getIdentity();

	IGuiGraphNode getParent();

	String[] getClasses();

	Map<String, Object> getData();

}
