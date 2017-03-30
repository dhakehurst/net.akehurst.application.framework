package net.akehurst.application.framework.engineering.common.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.technology.interfaceGui.data.diagram.GuiGraph;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.GuiGraphEdge;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.GuiGraphNode;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiDiagramData;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiGraph;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiGraphEdge;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiGraphNode;

public class GuiDiagramData implements IGuiDiagramData {

	/**
	 *
	 * @param styleReader
	 *            reads a .css file defining the diagram style
	 * @param layout
	 *            a json string defining the layout for the diagram
	 * @throws IOException
	 */
	public GuiDiagramData(final Reader styleReader) throws IOException {
		final BufferedReader br = new BufferedReader(styleReader);
		String line = br.readLine();
		final StringBuffer buf = new StringBuffer();
		while (null != line) {
			buf.append(line);
			buf.append(System.lineSeparator());
			line = br.readLine();
		}
		this.style = buf.toString();
		this.layout = new HashMap<>();
		this.graph = new GuiGraph();
	}

	GuiGraph graph;

	@Override
	public IGuiGraph getGraph() {
		return this.graph;
	}

	protected String style;

	@Override
	public String getStyle() {
		return this.style;
	}

	protected Map<String, Object> layout;

	@Override
	public Map<String, Object> getLayout() {
		return this.layout;
	}

	public IGuiGraphNode addNode(final IGuiGraphNode parent, final String identity, final String... types) {
		final IGuiGraphNode node = new GuiGraphNode(identity, parent, types);
		this.graph.getNodes().put(identity, node);
		return node;
	}

	public IGuiGraphEdge addEdge(final String identity, final IGuiGraphNode parent, final String sourceNodeId, final String targetNodeId,
			final String... types) {
		final IGuiGraphNode source = this.getGraph().getNodes().get(sourceNodeId);
		final IGuiGraphNode target = this.getGraph().getNodes().get(targetNodeId);

		final IGuiGraphEdge edge = new GuiGraphEdge(identity, parent, source, target, types);
		this.graph.getEdges().put(identity, edge);
		return edge;
	}
}
