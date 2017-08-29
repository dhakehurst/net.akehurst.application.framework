package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.List;
import java.util.Map;

public interface IGuiGraphEdge {

	String getIdentity();

	IGuiGraphNode getParent();

	IGuiGraphNode getSource();

	IGuiGraphNode getTarget();

	List<String> getClasses();

	Map<String, Object> getData();

}
