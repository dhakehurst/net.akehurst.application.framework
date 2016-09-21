package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.Map;

public interface IGuiGraphEdge {

	IGuiGraphNode getSource();

	IGuiGraphNode getTarget();

	String getLabel();

	Map<String, String> getData();

}
