package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.Map;

public interface IGuiGraphNode {

	String getIdentity();

	IGuiGraphNode getParent();

	Map<String, Object> getData();

	Map<String, Object> getProperties();

}
