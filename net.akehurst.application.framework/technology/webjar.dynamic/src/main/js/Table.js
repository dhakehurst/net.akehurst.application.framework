/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
 
 define([
//cloneEventsTo which is defined in Dynamic at present
], function(

) {

	function Table(tableId) {
		this.tableId = tableId
		this.table = $('#'+tableId).get(0)
		this.thead = this.table.createTHead()
		if (this.thead.rows.length < 1) {
			this.thead.insertRow(0)
		}
		this.headerRow = this.thead.rows[0]
		if (this.table.tBodies.length < 1) {
			this.tbody = this.table.createTBody()
		} else {
			this.tbody = this.table.tBodies[0]
		}
		
		this.rowTemplate = $(this.table).find('tr.table-row-template')
		if (this.rowTemplate.length==0) {
			$("<tr hidden='true' class='table-row-template'></tr>").appendTo($(this.thead))
			this.rowTemplate = $(this.table).find('tr.table-row-template')
		}
		$(this.rowTemplate).attr('hidden', true)
	}

	Table.prototype.addColumn = function(colHeaderContent, rowTemplateCellContent, existingRowCellContent) {
		try {
			$('<th>'+colHeaderContent+'</th>').appendTo($(this.headerRow))
			$('<td>'+rowTemplateCellContent+'</td>').appendTo($(this.rowTemplate))
			for(i=0; i< this.tbody.rows.length; i++) {
				$('<td>'+existingRowCellContent+'</td>').appendTo($(this.tbody.rows[i]))
			}
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}

	Table.prototype.clearAllColumnHeaders = function() {
		try {
			//template now moved to thead
			//$(table).find('tbody').find('tr').not('.table-row-template').remove()
			$(this.thead).find('tr').remove()
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}
	
	Table.prototype.appendRow = function(rowData) {
		try {
			if (this.rowTemplate.length ==0) {
				console.log('Error: table does not define a table-row-template ' + this.tableId)
			} else {
				let rowTemplateHtml = $(this.rowTemplate).get(0).outerHTML
				let row = rowData
				let tpl = eval('`'+rowTemplateHtml+'`');
				let tr = $(tpl)
				$(tr).removeAttr('hidden')
				$(tr).removeClass('table-row-template')
				$(tr).appendTo($(this.tbody))
				this.rowTemplate[0].cloneEventsTo(tr[0])
			}
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}

	Table.prototype.removeRow = function(rowId) {
		try {
			let dialog = $(this.table).closest('dialog')
			if (dialog.length==0) {
				$(this.table).find('#'+rowId).remove()
			} else {
				let dialog_id = $(dialog).attr('id')
				$(this.table).find('#'+dialog_id+'_'+rowId).remove()
			}
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}

	Table.prototype.clearAllRows = function() {
		try {
			//template now moved to thead
			//$(table).find('tbody').find('tr').not('.table-row-template').remove()
			$(this.tbody).find('tr').remove()
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}
	
	return Table

})