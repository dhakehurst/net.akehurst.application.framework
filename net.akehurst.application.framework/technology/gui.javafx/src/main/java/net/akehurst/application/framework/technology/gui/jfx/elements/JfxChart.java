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
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventType;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChart;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartData;
import net.akehurst.application.framework.technology.interfaceGui.data.chart.IGuiChartDataSeries;
import net.akehurst.application.framework.technology.interfaceGui.data.graph.IGuiGraphViewer;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

public class JfxChart<X, Y> implements IGuiChart<X, Y> {

	public JfxChart(final Chart jfx) {
		this.jfx = jfx;
	}

	@Override
	public void create(final UserSession session, final IGuiChart.Type chartType) {
		// TODO Auto-generated method stub

	}

	Chart jfx;

	@Override
	public IGuiChartData<X, Y> getData() {
		// TODO Auto-generated method stub
		return null;
	}

	class JxfChartData implements IGuiChartData<X, Y> {

		@Override
		public List<X> getXs() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<IGuiChartDataSeries<Y>> getSeries() {
			if (JfxChart.this.jfx instanceof XYChart<?, ?>) {
				final XYChart<X, Y> jfxChart = (XYChart<X, Y>) JfxChart.this.jfx;
				final ObservableList<XYChart.Series<X, Y>> jfxSeriesList = jfxChart.getData();

				return new AbstractList<IGuiChartDataSeries<Y>>() {
					@Override
					public int size() {
						return jfxSeriesList.size();
					}

					@Override
					public IGuiChartDataSeries<Y> get(final int index) {
						final XYChart.Series<X, Y> jfxSeries = jfxSeriesList.get(index);
						return JxfChartData.this.createSeries(jfxSeries);
					}
				};

			} else {
				return null;
			}
		}

		@Override
		public IGuiChartDataSeries<Y> getSeries(final String name) {
			if (JfxChart.this.jfx instanceof XYChart<?, ?>) {
				final XYChart<X, Y> jfxChart = (XYChart<X, Y>) JfxChart.this.jfx;
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
		public IGuiChartDataSeries<Y> addSeries(final String name) {
			if (JfxChart.this.jfx instanceof XYChart<?, ?>) {
				final XYChart<X, Y> jfxChart = (XYChart<X, Y>) JfxChart.this.jfx;
				final XYChart.Series<X, Y> s = new XYChart.Series<>();
				s.setName(name);
				Platform.runLater(() -> {
					jfxChart.getData().add(s);
					((XYChart) JfxChart.this.jfx).getXAxis().requestAxisLayout();
				});

				return this.createSeries(s);
			} else {
				return null;
			}
		}

		IGuiChartDataSeries<Y> createSeries(final XYChart.Series<X, Y> jfxSeries) {
			return new IGuiChartDataSeries<Y>() {

				@Override
				public String getName() {
					return jfxSeries.getName();
				}

				@Override
				public void setName(final String value) {
					jfxSeries.setName(value);
				}

				@Override
				public List<Y> getItems() {
					return new AbstractList<Y>() {
						@Override
						public int size() {
							return jfxSeries.getData().size();
						}

						@Override
						public Y get(final int index) {
							final XYChart.Data<X, Y> jfxDataItem = jfxSeries.getData().get(index);
							return jfxDataItem.getYValue();
						}

						@Override
						public void add(final int index, final Y element) {
							final XYChart.Data<X, Y> jfxData = new XYChart.Data<>();
							// jfxData.setXValue(element.getX());
							// jfxData.setYValue(element.getY());
							// Platform.runLater(() -> jfxDataList.add(index, jfxData));

						}
					};
				}

			};
		}

	}

	@Override
	public String getElementId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(final UserSession session, final String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(final UserSession session, final String propertyName, final Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addClass(final UserSession session, final String className) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeClass(final UserSession session, final String className) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEvent(final UserSession session, final GuiEventType eventType, final OnEventHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear(final UserSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisabled(final UserSession session, final boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoading(final UserSession session, final boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSubElement(final UserSession session, final String newElementId, final String newElementType, final String attributes,
			final Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSubElement(final UserSession session, final String subElementId) {
		// TODO Auto-generated method stub

	}

	@Override
	public IGuiText createText(final UserSession session, final String textId, final String content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGuiTable createTable(final UserSession session, final String tableId, final String content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGuiGraphViewer createGraph(final UserSession session, final String graphId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> IGuiChart<X, Y> createChart(final UserSession session, final String chartId, final String chartType, final String jsonChartData,
			final String jsonChartOptions) {
		// TODO Auto-generated method stub
		return null;
	}

}
