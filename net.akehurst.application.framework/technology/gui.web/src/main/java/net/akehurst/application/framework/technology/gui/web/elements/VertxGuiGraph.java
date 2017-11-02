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
package net.akehurst.application.framework.technology.gui.web.elements;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.json.JSONObject;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraph;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphEdge;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphNode;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphViewData;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphViewer;

public class VertxGuiGraph extends VertxGuiElement implements IGuiGraphViewer {

	public VertxGuiGraph(final IGuiRequest guiRequest, final IGuiScene scene, final IGuiDialog dialog, final String elementName) {
		super(guiRequest, scene, dialog, elementName);
	}

	@Override
	public void create(final UserSession session, final IGuiGraphViewData initialContent) {
		final String jsonDiagramData = this.createJsonString(initialContent);
		super.getGuiRequest().graphCreate(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), jsonDiagramData);
	}

	@Override
	public void update(final UserSession session, final IGuiGraphViewData newContent) {
		final String jsonDiagramData = this.createJsonString(newContent);
		super.getGuiRequest().graphUpdate(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), jsonDiagramData);
	}

	@Override
	public void remove(final UserSession session, final IGuiGraphViewData content) {
		super.getGuiRequest().graphRemove(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId());
	}

	String createJsonString(final IGuiGraphViewData graphData) {
		final JsonObject json = new JsonObject();

		json.add("elements", this.createElements(graphData.getGraph()));
		json.add("style", graphData.getStyle());
		json.add("layout", JsonValue.readJSON(JSONObject.wrap(graphData.getLayout()).toString()));
		json.add("options", JsonValue.readJSON(JSONObject.wrap(graphData.getOptions()).toString()));

		return json.toString();
	}

	private JsonArray createElements(final IGuiGraph graph) {
		final JsonArray jArr = new JsonArray();

		for (final IGuiGraphNode node : graph.getNodes().values()) {
			jArr.add(this.createNode(node));
		}

		for (final IGuiGraphEdge edge : graph.getEdges().values()) {
			jArr.add(this.createEdge(edge));
		}

		return jArr;
	}

	private JsonObject createNode(final IGuiGraphNode node) {
		final String nodeId = node.getIdentity();
		final JsonObject jnode = JsonValue.readJSON(JSONObject.wrap(node.getProperties()).toString()).asObject();
		jnode.add("group", "nodes");
		final JsonObject data = JsonValue.readJSON(JSONObject.wrap(node.getData()).toString()).asObject();
		data.add("id", nodeId);
		if (null != node.getParent()) {
			data.add("parent", node.getParent().getIdentity());
		}
		jnode.add("data", data);

		return jnode;
	}

	private JsonObject createEdge(final IGuiGraphEdge edge) {
		final String edgeId = edge.getIdentity();
		final String srcId = null == edge.getSource() ? "unknown" : edge.getSource().getIdentity();
		final String tgtId = null == edge.getTarget() ? "unknown" : edge.getTarget().getIdentity();
		final JsonObject jedge = new JsonObject();
		jedge.add("group", "edges");
		final JsonObject data = JsonValue.readJSON(JSONObject.wrap(edge.getData()).toString()).asObject();
		data.add("id", edgeId);
		data.add("source", srcId);
		data.add("target", tgtId);

		jedge.add("data", data);
		String classes = "";
		for (final String c : edge.getClasses()) {
			classes += c + " ";
		}
		jedge.add("classes", classes);
		return jedge;
	}
}
