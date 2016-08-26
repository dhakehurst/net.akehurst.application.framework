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
package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.util.AbstractList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartData;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartDataItem;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartDataSeries;

public class JfxChart implements IGuiChart {

	public JfxChart(final Chart jfx) {
		this.jfx = jfx;
	}

	Chart jfx;

	<X, Y> IGuiChartData<X, Y> createChartData(final List<XYChart.Data<X, Y>> jfxDataList) {
		return () -> new AbstractList<IGuiChartDataItem<X, Y>>() {
			@Override
			public int size() {
				return jfxDataList.size();
			}

			@Override
			public IGuiChartDataItem<X, Y> get(final int index) {
				final XYChart.Data<X, Y> jfxDataItem = jfxDataList.get(index);
				return new IGuiChartDataItem<X, Y>() {
					@Override
					public X getX() {
						return jfxDataItem.getXValue();
					}

					@Override
					public Y getY() {
						return jfxDataItem.getYValue();
					}

					@Override
					public String toString() {
						return "(" + this.getX() + "," + this.getY() + ")";
					}
				};
			}

			@Override
			public void add(final int index, final IGuiChartDataItem<X, Y> element) {
				final XYChart.Data<X, Y> jfxData = new XYChart.Data<>();
				jfxData.setXValue(element.getX());
				jfxData.setYValue(element.getY());
				Platform.runLater(() -> jfxDataList.add(index, jfxData));

			}
		};
	}

	<X, Y> IGuiChartDataSeries<X, Y> createSeries(final XYChart.Series<X, Y> jfxSeries) {
		return new IGuiChartDataSeries<X, Y>() {

			@Override
			public String getName() {
				return jfxSeries.getName();
			}

			@Override
			public void setName(final String value) {
				jfxSeries.setName(value);
			}

			@Override
			public IGuiChartData<X, Y> getData() {
				final ObservableList<XYChart.Data<X, Y>> jfxDataList = jfxSeries.getData();
				return JfxChart.this.createChartData(jfxDataList);
			}
		};
	}

	@Override
	public <X, Y> List<IGuiChartDataSeries<X, Y>> getSeries() {
		if (this.jfx instanceof XYChart<?, ?>) {
			final XYChart<X, Y> jfxChart = (XYChart<X, Y>) this.jfx;
			final ObservableList<XYChart.Series<X, Y>> jfxSeriesList = jfxChart.getData();

			return new AbstractList<IGuiChartDataSeries<X, Y>>() {
				@Override
				public int size() {
					return jfxSeriesList.size();
				}

				@Override
				public IGuiChartDataSeries<X, Y> get(final int index) {
					final XYChart.Series<X, Y> jfxSeries = jfxSeriesList.get(index);
					return JfxChart.this.createSeries(jfxSeries);
				}
			};

		} else {
			return null;
		}
	}

	@Override
	public <X, Y> IGuiChartDataSeries<X, Y> getSeries(final String name) {
		if (this.jfx instanceof XYChart<?, ?>) {
			final XYChart<X, Y> jfxChart = (XYChart<X, Y>) this.jfx;
			final Optional<XYChart.Series<X, Y>> jfxSeriesOpt = jfxChart.getData().stream().filter((s) -> s.getName().equals(name)).findFirst();
			if (jfxSeriesOpt.isPresent()) {
				final XYChart.Series<X, Y> jfxSeries = jfxSeriesOpt.get();
				return this.createSeries(jfxSeries);
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	@Override
	public <X, Y> IGuiChartDataSeries<X, Y> addSeries(final String name) {
		if (this.jfx instanceof XYChart<?, ?>) {
			final XYChart<X, Y> jfxChart = (XYChart<X, Y>) this.jfx;
			final XYChart.Series<X, Y> s = new XYChart.Series<>();
			s.setName(name);
			Platform.runLater(() -> {
				jfxChart.getData().add(s);
				((XYChart) this.jfx).getXAxis().requestAxisLayout();
			});

			return this.createSeries(s);
		} else {
			return null;
		}
	}
}
