package net.akehurst.application.framework.technology.guiInterface.elements;

import java.util.List;

public interface IGuiChartData<X,Y> {

	List<IGuiChartDataItem<X,Y>> getItems();
	
}
