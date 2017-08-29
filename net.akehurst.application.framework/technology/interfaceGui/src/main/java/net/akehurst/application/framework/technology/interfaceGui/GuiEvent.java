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
package net.akehurst.application.framework.technology.interfaceGui;

import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.data.table.AbstractGuiTableData;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTableData;
import net.akehurst.application.framework.technology.interfaceGui.data.table.IGuiTableRow;

public class GuiEvent {

	public GuiEvent(final UserSession session, final GuiEventSignature signature, final Map<String, Object> eventData) {
		this.session = session;
		this.signature = signature;
		this.eventData = eventData;
	}

	UserSession session;

	public UserSession getSession() {
		return this.session;
	}

	GuiEventSignature signature;

	public GuiEventSignature getSignature() {
		return this.signature;
	}

	Map<String, Object> eventData;

	public Map<String, Object> getEventData() {
		return this.eventData;
	}

	public <T> T getDataItem(final String key) {
		return (T) this.eventData.get(key);
	}

	public IGuiTableData getTableData(final String key) {

		final Map<String, Object> tableData = this.getDataItem(key);
		final List<String> headerData = (List<String>) tableData.get("afHeaders");
		final List<String> rowIds = (List<String>) tableData.get("afRowIds");
		final List<List<Map<String, Object>>> rowData = (List<List<Map<String, Object>>>) tableData.get("afRows");

		final IGuiTableData td = new AbstractGuiTableData() {
			@Override
			public List<String> getColumnIds() {
				return headerData;
			}

			@Override
			public List<String> getRowIds() {
				return rowIds;
			}

			@Override
			public IGuiTableRow getRowData(final String rowId) {
				final int index = this.getRowIds().indexOf(rowId);
				return this.createRow(rowId, rowData.get(index));
			}

		};

		return td;
	}
}
