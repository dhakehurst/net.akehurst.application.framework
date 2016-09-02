package net.akehurst.application.framework.technology.interfaceGui.data.table;

import java.util.AbstractList;
import java.util.List;

abstract public class AbstractGuiTableData<C, R> implements IGuiTableData<C, R> {

	@Override
	public List<IGuiTableRow<C, R>> getRows() {
		return new AbstractList<IGuiTableRow<C, R>>() {

			@Override
			public IGuiTableRow<C, R> get(final int index) {

				return () -> AbstractGuiTableData.this.getRowData(index);
			}

			@Override
			public int size() {
				return AbstractGuiTableData.this.getNumberOfRows();
			}
		};

	}

}
