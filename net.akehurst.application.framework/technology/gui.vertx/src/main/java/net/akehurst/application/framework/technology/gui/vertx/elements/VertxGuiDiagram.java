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
package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.util.Map;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiDiagram;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiDiagramData;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiGraphEdge;
import net.akehurst.application.framework.technology.interfaceGui.data.diagram.IGuiGraphNode;

public class VertxGuiDiagram extends VertxGuiElement implements IGuiDiagram {

	public VertxGuiDiagram(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		super(guiRequest, scene, elementName);
	}

	@Override
	public void add(final UserSession session, final IGuiDiagramData content) {
		final String jsonDiagramData = this.createJsonString(content);
		super.guiRequest.addDiagram(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, jsonDiagramData);
	}

	@Override
	public void remove(final UserSession session, final IGuiDiagramData content) {
		// TODO Auto-generated method stub

	}

	String createJsonString(final IGuiDiagramData diagramData) {
		final JsonObject json = new JsonObject();

		json.put("elements", this.createElements(diagramData));
		json.put("style", diagramData.getStyle());

		return json.encode();
	}

	private JsonArray createElements(final IGuiDiagramData diagramData) {
		final JsonArray jArr = new JsonArray();

		for (final IGuiGraphNode node : diagramData.getGraph().getNodes().values()) {
			jArr.add(this.createNode(node));
		}

		for (final IGuiGraphEdge edge : diagramData.getGraph().getEdges().values()) {
			jArr.add(this.createEdge(edge));
		}

		return jArr;
	}

	private JsonObject createNode(final IGuiGraphNode node) {
		final String nodeId = node.getIdentity();
		final JsonObject jnode = new JsonObject();
		jnode.put("group", "nodes");
		final JsonObject data = new JsonObject();
		data.put("id", nodeId);
		if (null != node.getParent()) {
			data.put("parent", node.getParent().getIdentity());
		}
		for (final Map.Entry<String, String> me : node.getData().entrySet()) {
			data.put(me.getKey(), me.getValue());
		}

		jnode.put("data", data);
		String classes = "";
		for (final String c : node.getType()) {
			classes += c + " ";
		}
		jnode.put("classes", classes);
		return jnode;
	}

	private JsonObject createEdge(final IGuiGraphEdge edge) {
		final String edgeId = edge.getIdentity();
		final String srcId = edge.getSource().getIdentity();
		final String tgtId = edge.getTarget().getIdentity();
		final JsonObject jedge = new JsonObject();
		jedge.put("group", "edges");
		final JsonObject data = new JsonObject();
		data.put("id", edgeId);
		data.put("source", srcId);
		data.put("target", tgtId);

		for (final Map.Entry<String, String> me : edge.getData().entrySet()) {
			data.put(me.getKey(), me.getValue());
		}

		jedge.put("data", data);

		return jedge;
	}
}
