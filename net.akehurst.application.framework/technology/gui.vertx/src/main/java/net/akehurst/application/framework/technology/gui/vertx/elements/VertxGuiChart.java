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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartData;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartDataSeries;

public class VertxGuiChart<X, Y> extends VertxGuiElement implements IGuiChart<X, Y> {

	public VertxGuiChart(final IGuiRequest guiRequest, final IGuiScene scene, final IGuiDialog dialog, final String elementName) {
		super(guiRequest, scene, dialog, elementName);
		this.data = new VertxGuiChartData<>();
	}

	private String getChartId() {
		return this.getElementId() + "_chart";
	}

	@Override
	public void create(final UserSession session, final IGuiChart.Type chartType) {
		this.createChartContent(session, chartType);
	}

	IGuiChartData<X, Y> data;

	@Override
	public IGuiChartData<X, Y> getData() {
		return this.data;
	}

	private void createChartContent(final UserSession session, final IGuiChart.Type chartType) {
		final JsonObject chartData = this.createJsonData();
		final JsonObject chartOptions = new JsonObject();

		final String chartId = this.getChartId();

		final String jsonChartDataStr = chartData.toString();
		final String jsonChartOptions = chartOptions.toString();

		final String parentId = this.getElementId();
		final String chartTypeStr = this.getChartTypeName(chartType);
		this.getGuiRequest().chartCreate(session, this.getScene().getStageId(), this.getScene().getSceneId(), parentId, chartId, chartTypeStr, jsonChartDataStr,
				jsonChartOptions);
	}

	// these depend on the underlying js side chart library - currently Chart.js

	private String getChartTypeName(final IGuiChart.Type type) {
		switch (type) {
			case Bar:
				return "bar";
			case Bubble:
				return "bubble";
			case Doughnut:
				return "doughnut";
			case Line:
				return "line";
			case Pie:
				return "pie";
			case PolarArea:
				return "polarArea";
			case Radar:
				return "radar";
			case XY:
				return "line";
			default:
				return "";

		}
	}

	private JsonObject createJsonData() {
		final JsonObject data = new JsonObject();
		data.put("datasets", this.getDatasets());
		data.put("labels", new JsonArray(this.getData().getXs()));
		// data.put("xLabels", new JsonArray(Arrays.asList("A", "B", "C", "D", "E")));
		// data.put("yLabels", new JsonArray());
		return data;
	}

	private JsonArray getDatasets() {
		final JsonArray arr = new JsonArray();
		for (final IGuiChartDataSeries<Y> s : this.getData().getSeries()) {
			final JsonObject jsonSeries = new JsonObject();
			arr.add(jsonSeries);
			jsonSeries.put("label", s.getName());
			final JsonArray dataArray = new JsonArray();
			jsonSeries.put("data", dataArray);
			for (final Y item : s.getItems()) {
				dataArray.add(item);
			}
		}
		return arr;
	}

	private void addDataItem(final String seriesName, final X x, final Y y) {
		// this.guiRequest.chartAddDataItem(session, VertxGuiChart.this.scene.getStageId(), VertxGuiChart.this.scene.getSceneId(), this.getChartId(),
		// seriesName, x, y);
	}

}
