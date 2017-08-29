package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.List;
import java.util.Map;

public interface IGuiGraphNode {

	String getIdentity();

	IGuiGraphNode getParent();

	List<String> getClasses();

	Map<String, Object> getData();

}
