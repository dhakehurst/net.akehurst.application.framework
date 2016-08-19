package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.util.AbstractList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiChart;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiChartData;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiChartDataItem;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiChartDataSeries;

public class JfxChart implements IGuiChart {

	public JfxChart(Chart jfx) {
		this.jfx = jfx;
	}

	Chart jfx;

	<X, Y> IGuiChartData<X, Y> createChartData(List<XYChart.Data<X, Y>> jfxDataList) {
		return new IGuiChartData<X, Y>() {

			@Override
			public List<IGuiChartDataItem<X, Y>> getItems() {
				return new AbstractList<IGuiChartDataItem<X, Y>>() {
					@Override
					public int size() {
						return jfxDataList.size();
					}

					@Override
					public IGuiChartDataItem<X, Y> get(int index) {
						XYChart.Data<X, Y> jfxDataItem = jfxDataList.get(index);
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
					public void add(int index, IGuiChartDataItem<X, Y> element) {
						XYChart.Data<X, Y> jfxData = new XYChart.Data<X, Y>();
						jfxData.setXValue(element.getX());
						jfxData.setYValue(element.getY());
						Platform.runLater(() -> jfxDataList.add(index, jfxData));

					}
				};
			}
		};
	}

	<X, Y> IGuiChartDataSeries<X, Y> createSeries(XYChart.Series<X, Y> jfxSeries) {
		return new IGuiChartDataSeries<X, Y>() {

			@Override
			public String getName() {
				return jfxSeries.getName();
			}

			@Override
			public void setName(String value) {
				jfxSeries.setName(value);
			}

			@Override
			public IGuiChartData<X, Y> getData() {
				ObservableList<XYChart.Data<X, Y>> jfxDataList = jfxSeries.getData();
				return createChartData(jfxDataList);
			}
		};
	}

	@Override
	public <X, Y> List<IGuiChartDataSeries<X, Y>> getSeries() {
		if (this.jfx instanceof XYChart<?, ?>) {
			XYChart<X, Y> jfxChart = (XYChart<X, Y>) jfx;
			ObservableList<XYChart.Series<X, Y>> jfxSeriesList = jfxChart.getData();

			return new AbstractList<IGuiChartDataSeries<X, Y>>() {
				@Override
				public int size() {
					return jfxSeriesList.size();
				}

				@Override
				public IGuiChartDataSeries<X, Y> get(int index) {
					XYChart.Series<X, Y> jfxSeries = jfxSeriesList.get(index);
					return createSeries(jfxSeries);
				}
			};

		} else {
			return null;
		}
	}

	@Override
	public <X, Y> IGuiChartDataSeries<X, Y> getSeries(String name) {
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
	public <X, Y> IGuiChartDataSeries<X, Y> addSeries(String name) {
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
