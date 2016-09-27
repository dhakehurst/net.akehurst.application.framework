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

import java.util.List;

import io.vertx.core.json.JsonObject;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartData;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartDataSeries;

public class VertxGuiChart extends VertxGuiElement implements IGuiChart {

	public VertxGuiChart(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		super(guiRequest, scene, elementName);
	}

	JsonObject chartData;

	@Override
	public <X, Y> List<IGuiChartDataSeries<X, Y>> getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> IGuiChartDataSeries<X, Y> getSeries(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> IGuiChartDataSeries<X, Y> addSeries(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	void createChartContent(final UserSession session, final String chartType, final JsonObject chartData, final JsonObject chartOptions) {

		final String chartId = this.elementName + "_chart";

		final String jsonChartDataStr = chartData.toString();
		final String jsonChartOptions = chartOptions.toString();

		final String parentId = this.elementName;
		this.guiRequest.addChart(session, this.scene.getStageId(), this.scene.getSceneId(), parentId, chartId, chartType, jsonChartDataStr, jsonChartOptions);
	}

	static class DataSeries<X, Y> implements IGuiChartDataSeries<X, Y> {

		String name;

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public void setName(final String value) {
			this.name = this.name;
		}

		@Override
		public IGuiChartData<X, Y> getData() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
