package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.Map;

public interface IGuiGraph {

	Map<String, IGuiGraphNode> getNodes();

	Map<String, IGuiGraphEdge> getEdges();

}
