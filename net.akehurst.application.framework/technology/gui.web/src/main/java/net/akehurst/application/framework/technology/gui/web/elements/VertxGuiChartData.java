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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartData;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartDataSeries;

public class VertxGuiChartData<X, Y> implements IGuiChartData<X, Y> {

	public VertxGuiChartData() {
		this.xs = new ArrayList<>();
		this.series = new HashMap<>();
	}

	List<X> xs;

	@Override
	public List<X> getXs() {
		return this.xs;
	}

	Map<String, IGuiChartDataSeries<Y>> series;

	@Override
	public List<IGuiChartDataSeries<Y>> getSeries() {
		return new ArrayList<>(this.series.values());
	}

	@Override
	public IGuiChartDataSeries<Y> getSeries(final String name) {
		return this.series.get(name);
	}

	@Override
	public IGuiChartDataSeries<Y> addSeries(final String name) {
		final IGuiChartDataSeries<Y> series = new DataSeries<>(name);
		this.series.put(name, series);
		return series;
	}

	class DataSeries<Y> implements IGuiChartDataSeries<Y> {

		public DataSeries(final String name) {
			this.name = name;
			this.items = new ArrayList<>();
			this.colours = new ArrayList<>();
		}

		private String name;
		private final List<Y> items;
		private final List<String> colours;

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public void setName(final String value) {
			this.name = value;
		}

		@Override
		public List<Y> getItems() {
			// TODO: handle dynamic adding of elements
			return this.items;
		}

		@Override
		public List<String> getColours() {
			// TODO: handle dynamic adding of elements
			return this.colours;
		}
	}

}
