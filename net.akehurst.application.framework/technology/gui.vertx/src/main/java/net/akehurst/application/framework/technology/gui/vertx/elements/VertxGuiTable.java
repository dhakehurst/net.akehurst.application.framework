package net.akehurst.application.framework.technology.gui.vertx.elements;

import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.technology.guiInterface.IGuiRequest;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene;
import net.akehurst.application.framework.technology.guiInterface.data.table.IGuiTable;
import net.akehurst.application.framework.technology.guiInterface.data.table.IGuiTableCell;
import net.akehurst.application.framework.technology.guiInterface.data.table.IGuiTableData;
import net.akehurst.application.framework.technology.guiInterface.data.table.IGuiTableRow;

public class VertxGuiTable extends VertxGuiElement implements IGuiTable {

	public VertxGuiTable(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		super(guiRequest, scene, elementName);
	}

	@Override
	public <C, R> void setData(final UserSession session, final IGuiTableData<C, R> data) {
		String tableData = "";

		for (final IGuiTableRow<C, R> row : data.getRows()) {
			tableData += "<tr>";
			for (final IGuiTableCell<C, R> cell : row.getCells()) {
				tableData += "<td>" + cell.getValue() + "</td>";
			}
			tableData += "</tr>";
		}
		final String newElementId = this.elementName + "_tbody";
		this.guiRequest.addElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, newElementId, "tbody", "{}", tableData);
	}
}
