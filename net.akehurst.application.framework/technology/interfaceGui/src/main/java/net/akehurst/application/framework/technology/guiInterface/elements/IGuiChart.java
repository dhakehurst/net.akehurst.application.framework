package net.akehurst.application.framework.technology.guiInterface.elements;

import java.util.List;


public interface IGuiChart {

	<X,Y> List<IGuiChartDataSeries<X,Y>> getSeries();
	<X,Y> IGuiChartDataSeries<X,Y> getSeries(String name);
	<X,Y> IGuiChartDataSeries<X,Y> addSeries(String name);
}
