package net.akehurst.application.framework.technology.guiInterface.elements;

public interface IGuiChartDataSeries<X,Y> {

	String getName();
	void setName(String value);
	
	IGuiChartData<X,Y> getData();
	
}
