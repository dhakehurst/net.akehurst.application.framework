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

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTable;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTableData;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTableRow;

public class VertxGuiTable extends VertxGuiElement implements IGuiTable {

	public VertxGuiTable(final IGuiRequest guiRequest, final IGuiScene scene, final IGuiDialog dialog, final String elementName) {
		super(guiRequest, scene, dialog, elementName);
	}

	@Override
	public void create(final UserSession session) {
		this.getGuiRequest().tableCreate(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId());
	}

	@Override
	public void remove(final UserSession session) {
		this.getGuiRequest().tableRemove(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId());
	}

	@Override
	public void setData(final UserSession session, final IGuiTableData data) {
		for (final IGuiTableRow row : data.getRows()) {
			// final List<Map<String, Object>> row = row.getRowData();
			final Map<String, Object> rowData = new HashMap<>();
			for (final String columnId : data.getColumnIds()) {
				rowData.put(columnId, row.getDataForColumn(columnId));
			}
			this.appendRow(session, rowData);
		}
	}

	@Override
	public void addColumn(final UserSession session, final String colHeaderContent, final String rowTemplateCellContent, final String existingRowCellContent) {
		this.getGuiRequest().tableAddColumn(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), colHeaderContent,
				rowTemplateCellContent, existingRowCellContent);
	}

	@Override
	public void clearAllColumnHeaders(final UserSession session) {
		this.getGuiRequest().tableClearAllColumnHeaders(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId());
	}

	@Override
	public void appendRow(final UserSession session, final Map<String, Object> rowData) {
		this.getGuiRequest().tableAppendRow(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), rowData);
	}

	@Override
	public void removeRow(final UserSession session, final String rowId) {
		this.getGuiRequest().tableRemoveRow(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), rowId);
	}

	@Override
	public void clearAllRows(final UserSession session) {
		this.getGuiRequest().tableClearAllRows(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId());
	}
}
