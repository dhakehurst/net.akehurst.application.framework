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
package net.akehurst.application.framework.technology.interfaceGui.data.table;

import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public interface IGuiTable extends IGuiElement {

	void create(UserSession session);

	void remove(UserSession session);

	void setData(UserSession session, IGuiTableData data);

	void addColumn(UserSession session, String colHeaderContent, String rowTemplateCellContent, String existingRowCellContent);

	void appendRow(UserSession session, Map<String, Object> rowData);

	void removeRow(UserSession session, String rowId);

	void clearAllRows(UserSession session);

	void clearAllColumnHeaders(UserSession session);
}
