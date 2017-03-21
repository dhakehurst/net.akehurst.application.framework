package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.Map;

public interface IGuiGraphEdge {

	String getIdentity();

	IGuiGraphNode getParent();

	IGuiGraphNode getSource();

	IGuiGraphNode getTarget();

	String[] getType();

	Map<String, String> getData();

}
