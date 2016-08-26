package net.akehurst.application.framework.technology.guiInterface.data.table;

import java.util.List;

public interface IGuiTableRow<C, R> {

	List<IGuiTableCell<C, R>> getCells();

}
