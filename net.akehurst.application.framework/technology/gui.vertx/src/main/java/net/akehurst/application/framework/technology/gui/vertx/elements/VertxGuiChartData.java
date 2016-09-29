package net.akehurst.application.framework.technology.gui.vertx.elements;

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
		}

		String name;

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public void setName(final String value) {
			this.name = value;
		}

		final List<Y> items = new ArrayList<>();

		@Override
		public List<Y> getItems() {
			// TODO: handle dynamic adding of elements
			return this.items;
		}

	}

}
