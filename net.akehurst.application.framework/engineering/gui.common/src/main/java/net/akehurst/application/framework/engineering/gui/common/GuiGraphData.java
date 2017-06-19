/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.engineering.gui.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.technology.interfaceGui.data.graph.GuiGraph;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.GuiGraphEdge;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.GuiGraphNode;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraph;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphEdge;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphNode;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphViewData;

public class GuiGraphData implements IGuiGraphViewData {

	public GuiGraphData() {
		this.style = "";
		this.layout = new HashMap<>();
		this.options = new HashMap<>();
		this.graph = new GuiGraph();
	}

	/**
	 *
	 * @param styleReader
	 *            reads a .css file defining the diagram style
	 * @param layout
	 *            a json string defining the layout for the diagram
	 * @throws IOException
	 */
	public GuiGraphData(final Reader styleReader) throws IOException {
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
		this.options = new HashMap<>();
		this.graph = new GuiGraph();
	}

	protected final GuiGraph graph;
	protected final String style;
	protected final Map<String, Object> layout;
	protected final Map<String, Object> options;

	@Override
	public IGuiGraph getGraph() {
		return this.graph;
	}

	@Override
	public String getStyle() {
		return this.style;
	}

	@Override
	public Map<String, Object> getLayout() {
		return this.layout;
	}

	@Override
	public Map<String, Object> getOptions() {
		return this.options;
	}

	public IGuiGraphNode addNode(final IGuiGraphNode parent, final String identity, final String... classes) {
		final IGuiGraphNode node = new GuiGraphNode(identity, parent, classes);
		this.graph.getNodes().put(identity, node);
		return node;
	}

	public IGuiGraphEdge addEdge(final String identity, final IGuiGraphNode parent, final String sourceNodeId, final String targetNodeId,
			final String... classes) {
		final IGuiGraphEdge edge = new GuiGraphEdge(this.getGraph(), identity, parent, sourceNodeId, targetNodeId, classes);
		this.graph.getEdges().put(identity, edge);
		return edge;
	}
}
