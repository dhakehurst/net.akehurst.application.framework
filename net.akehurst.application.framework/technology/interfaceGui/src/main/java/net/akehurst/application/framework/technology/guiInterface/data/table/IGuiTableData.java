package net.akehurst.application.framework.technology.guiInterface.data.table;

import java.util.List;

public interface IGuiTableData<C, R> {

	void getCellData(C column, R row);

	List<IGuiTableRow<C, R>> getRows();

}
