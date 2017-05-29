package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.Map;

public interface IGuiGraphViewData {
	IGuiGraph getGraph();

	String getStyle();

	Map<String, Object> getLayout();
}
