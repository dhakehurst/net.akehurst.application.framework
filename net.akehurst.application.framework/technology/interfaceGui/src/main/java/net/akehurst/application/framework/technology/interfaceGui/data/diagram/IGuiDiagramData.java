package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.Map;

public interface IGuiDiagramData {

	IGuiGraph getGraph();

	String getStyle();

	Map<String, Object> getLayout();
}
