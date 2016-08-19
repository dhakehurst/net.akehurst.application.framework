package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.util.List;

import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiChart;
import net.akehurst.application.framework.technology.guiInterface.elements.IGuiChartDataSeries;

public class VertxGuiChart extends VertxGuiElement implements IGuiChart {

	public VertxGuiChart(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		super(guiRequest, scene, elementName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public <X, Y> List<IGuiChartDataSeries<X, Y>> getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> IGuiChartDataSeries<X, Y> getSeries(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> IGuiChartDataSeries<X, Y> addSeries(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
