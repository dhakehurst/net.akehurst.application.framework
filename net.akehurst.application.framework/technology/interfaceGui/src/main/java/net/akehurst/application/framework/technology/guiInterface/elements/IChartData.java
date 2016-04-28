package net.akehurst.application.framework.technology.guiInterface.elements;

import java.util.List;

public interface IChartData<X,Y> {

	List<IChartDataItem<X,Y>> getItems();
	
}
