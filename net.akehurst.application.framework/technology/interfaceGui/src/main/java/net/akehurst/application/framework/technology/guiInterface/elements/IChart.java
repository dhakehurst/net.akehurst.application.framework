package net.akehurst.application.framework.technology.guiInterface.elements;

import java.util.List;


public interface IChart {

	<X,Y> List<IChartDataSeries<X,Y>> getSeries();
	<X,Y> IChartDataSeries<X,Y> getSeries(String name);
	<X,Y> IChartDataSeries<X,Y> addSeries(String name);
}
