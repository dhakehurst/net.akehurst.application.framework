package net.akehurst.application.framework.technology.guiInterface.data.table;

import net.akehurst.application.framework.common.UserSession;

public interface IGuiTable {

	<C, R> void setData(UserSession session, IGuiTableData<C, R> data);

}
