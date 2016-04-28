package net.akehurst.application.framework.technology.guiInterface.elements;

public interface IChartDataSeries<X,Y> {

	String getName();
	void setName(String value);
	
	IChartData<X,Y> getData();
	
}
