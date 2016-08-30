package net.akehurst.application.framework.technology.interfaceGui.data.table;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

abstract public class AbstractGuiTableData<C, R> implements IGuiTableData<C, R> {

	@Override
	public List<IGuiTableRow<C, R>> getRows() {
		return new AbstractList<IGuiTableRow<C, R>>() {

			@Override
			public IGuiTableRow<C, R> get(final int index) {

				return new IGuiTableRow<C, R>() {

					@Override
					public R getRow() {
						return AbstractGuiTableData.this.getRowIndex(index);
					}

					@Override
					public List<IGuiTableCell<C, R>> getCells() {
						final List<IGuiTableCell<C, R>> result = new ArrayList<>();
						for (final C column : AbstractGuiTableData.this.getColumnIndices()) {
							final R row = AbstractGuiTableData.this.getRowIndex(index);
							result.add(AbstractGuiTableData.this.getCellData(column, row));
						}
						return result;
					}

				};
			}

			@Override
			public int size() {
				return AbstractGuiTableData.this.getNumberOfRows();
			}
		};

	}

}
