/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTableData;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTableRow;

public class VertxGuiTable extends VertxGuiElement implements IGuiTable {

	public VertxGuiTable(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		super(guiRequest, scene, elementName);
	}

	@Override
	public <C, R> void setData(final UserSession session, final IGuiTableData<C, R> data) {
		// String tableData = "";
		// for (final IGuiTableRow<C, R> row : data.getRows()) {
		// final String rowId = this.elementName + "-" + row.getRow();
		// tableData += "<tr id='" + rowId + "' >";
		// for (final IGuiTableCell<C, R> cell : row.getCells()) {
		// final String cellId = rowId + "-" + cell.getColumn();
		// tableData += "<td id='" + cellId + "' >" + cell.getValue() + "</td>";
		// }
		// tableData += "</tr>";
		// }
		// final String newElementId = this.elementName + "_tbody";
		// this.guiRequest.addElement(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, newElementId, "tbody", "{}", tableData);

		for (final IGuiTableRow<C, R> row : data.getRows()) {
			final Map<String, Object> rowData = row.getData();
			this.appendRow(session, rowData);
		}

	}

	@Override
	public void appendRow(final UserSession session, final Map<String, Object> rowData) {
		this.guiRequest.tableAppendRow(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, rowData);
	}
}
