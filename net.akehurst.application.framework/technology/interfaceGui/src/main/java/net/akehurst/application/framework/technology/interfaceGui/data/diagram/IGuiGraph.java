package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.List;

public interface IGuiGraph {

	List<IGuiGraphNode> getNodes();

	List<IGuiGraphEdge> getEdges();

}
