package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.Map;

public interface IGuiGraph {

	Map<String, IGuiGraphNode> getNodes();

	Map<String, IGuiGraphEdge> getEdges();

}
