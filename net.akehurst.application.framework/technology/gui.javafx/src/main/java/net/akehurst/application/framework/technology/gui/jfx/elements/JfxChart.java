package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.util.AbstractList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import net.akehurst.application.framework.technology.guiInterface.elements.IChart;
import net.akehurst.application.framework.technology.guiInterface.elements.IChartData;
import net.akehurst.application.framework.technology.guiInterface.elements.IChartDataItem;
import net.akehurst.application.framework.technology.guiInterface.elements.IChartDataSeries;

public class JfxChart implements IChart {

	public JfxChart(Chart jfx) {
		this.jfx = jfx;
	}

	Chart jfx;

	<X, Y> IChartData<X, Y> createChartData(List<XYChart.Data<X, Y>> jfxDataList) {
		return new IChartData<X, Y>() {

			@Override
			public List<IChartDataItem<X, Y>> getItems() {
				return new AbstractList<IChartDataItem<X, Y>>() {
					@Override
					public int size() {
						return jfxDataList.size();
					}

					@Override
					public IChartDataItem<X, Y> get(int index) {
						XYChart.Data<X, Y> jfxDataItem = jfxDataList.get(index);
						return new IChartDataItem<X, Y>() {
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
					public void add(int index, IChartDataItem<X, Y> element) {
						XYChart.Data<X, Y> jfxData = new XYChart.Data<X, Y>();
						jfxData.setXValue(element.getX());
						jfxData.setYValue(element.getY());
						Platform.runLater(() -> jfxDataList.add(index, jfxData));

					}
				};
			}
		};
	}

	<X, Y> IChartDataSeries<X, Y> createSeries(XYChart.Series<X, Y> jfxSeries) {
		return new IChartDataSeries<X, Y>() {

			@Override
			public String getName() {
				return jfxSeries.getName();
			}

			@Override
			public void setName(String value) {
				jfxSeries.setName(value);
			}

			@Override
			public IChartData<X, Y> getData() {
				ObservableList<XYChart.Data<X, Y>> jfxDataList = jfxSeries.getData();
				return createChartData(jfxDataList);
			}
		};
	}

	@Override
	public <X, Y> List<IChartDataSeries<X, Y>> getSeries() {
		if (this.jfx instanceof XYChart<?, ?>) {
			XYChart<X, Y> jfxChart = (XYChart<X, Y>) jfx;
			ObservableList<XYChart.Series<X, Y>> jfxSeriesList = jfxChart.getData();

			return new AbstractList<IChartDataSeries<X, Y>>() {
				@Override
				public int size() {
					return jfxSeriesList.size();
				}

				@Override
				public IChartDataSeries<X, Y> get(int index) {
					XYChart.Series<X, Y> jfxSeries = jfxSeriesList.get(index);
					return createSeries(jfxSeries);
				}
			};

		} else {
			return null;
		}
	}

	@Override
	public <X, Y> IChartDataSeries<X, Y> getSeries(String name) {
		if (this.jfx instanceof XYChart<?, ?>) {
			XYChart<X, Y> jfxChart = (XYChart<X, Y>) jfx;
			Optional<XYChart.Series<X, Y>> jfxSeriesOpt = jfxChart.getData().stream().filter((s) -> s.getName().equals(name)).findFirst();
			if (jfxSeriesOpt.isPresent()) {
				XYChart.Series<X, Y> jfxSeries = jfxSeriesOpt.get();
				return createSeries(jfxSeries);
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	@Override
	public <X, Y> IChartDataSeries<X, Y> addSeries(String name) {
		if (this.jfx instanceof XYChart<?, ?>) {
			XYChart<X, Y> jfxChart = (XYChart<X, Y>) jfx;
			XYChart.Series<X, Y> s = new XYChart.Series<X, Y>();
			s.setName(name);
			Platform.runLater(() -> {
				jfxChart.getData().add(s);
				((XYChart) jfx).getXAxis().requestAxisLayout();
			});
			
			return createSeries(s);
		} else {
			return null;
		}
	}
}
