package net.akehurst.application.framework.technology.interfaceGui.data.table;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class AbstractGuiTableData implements IGuiTableData {

    protected IGuiTableRow createRow(final String rowId, final List<Map<String, Object>> rowData) {
        return new IGuiTableRow() {

            @Override
            public String getId() {
                return rowId;
            }

            @Override
            public List<Map<String, Object>> getRowData() {
                return rowData;
            }

            @Override
            public Map<String, Object> getDataForColumn(final String columnId) {
                final int index = AbstractGuiTableData.this.getColumnIds().indexOf(columnId);
                return rowData.get(index);
            }
        };
    }

    @Override
    public int getNumberOfRows() {
        return this.getRowIds().size();
    }

    @Override
    public List<IGuiTableRow> getRows() {
        return this.getRowIds().stream().map(rowId -> this.getRowData(rowId)).collect(Collectors.toList());
    }

}
