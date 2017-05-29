package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.HashMap;
import java.util.Map;

public class GuiGraph implements IGuiGraph {

	public GuiGraph() {
		this.nodes = new HashMap<>();
		this.edges = new HashMap<>();
	}

	private final Map<String, IGuiGraphNode> nodes;
	private final Map<String, IGuiGraphEdge> edges;

	@Override
	public Map<String, IGuiGraphNode> getNodes() {
		return this.nodes;
	}

	@Override
	public Map<String, IGuiGraphEdge> getEdges() {
		return this.edges;
	}

}
