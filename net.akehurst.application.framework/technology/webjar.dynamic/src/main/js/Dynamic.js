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
 
define([
	"jquery",
	"ServerComms",
	"crypto.pbkdf2",
	"crypto.aes"
], function($, ServerComms, cryptoPBK, cryptoAES) {
 
	Element.prototype.cloneEventsTo = function(clone) {
	
			let events = $._data(this,'events')
			for(let type in events) {
				$.each(events[type], function(ix, h) {
					$.event.add(clone, type, h.handler, h.data)
				})
			}
	
		let origEls = this.getElementsByTagName('*')
		let cloneEls = clone.getElementsByTagName('*')
		
		for(let i=0; i<origEls.length; i++) {
			let o = origEls[i]
			let c = cloneEls[i]
			let events = jQuery._data(o,'events')
			for(let type in events) {
				$.each(events[type], function(ix, h) {
					$.event.add(c, type, h.handler, h.data)
				})
			}
		} //for
	}
 
	function Dynamic(rootPath, stageId) {
	
		if (typeof stageId === 'undefined' || null===stageId) { //may have an empty ("") stage id
			alert("var stageId must be given a value")
		}
		if (typeof rootPath === 'undefined' || null===rootPath) {
		  alert("var rootPath must be given a value")
		}
		var expectedStart = '/'
		if (rootPath.length > 0) {
			expectedStart = rootPath+'/'
		}
		if (stageId.length > 0) {
			expectedStart += stageId+'/'
		}
		
		var path = window.location.pathname
		
		if (!path.startsWith(expectedStart)) {
			alert("'rootPath +stageId' must have a value that matches the start of the url path, currently expectedStart = '"+expectedStart+"'")
		}
		
		var sceneId = path.substring(expectedStart.length, path.length-1); //pick sceneId out of path and loose the leading and trailing '/'
		if (sceneId === '/') {
		  sceneId = "" //handle special case
		}
		
		console.log("sceneId="+sceneId)
	
		this.rootPath = rootPath
		this.stageId = stageId
		this.sceneId = sceneId
		this.initComms(expectedStart);
		
		// cache created editors so we can find them again
		this.editors = {}
		this.diagrams = {}
		this.highlighter = {}
	}
	
	Dynamic.prototype.fetchEventData = function(el) {
	//TODO: not sure if this is quite what we want!
		var d1 = this.fetchEventData1(el,'tr')
		var d2 = this.fetchEventData1(el,'fieldset') //legacy now replaced with div.event-group (mainly because browser support for fieldset css/flex is broken on some browsers)
		var d3 = this.fetchEventData1(el,'event-group')
		var data = $.extend({}, d1, d2, d3)
		return data
	}
	Dynamic.prototype.fetchEventData1 = function(el, type) {
		let data = {}
		let type_or_class = type+', .'+type
		let p = el.closest(type_or_class)
		if (null!=this.form) {
			for(i=0; i< this.form.length; i++) {
				let id = this.form[i].id
				let value = this.form[i].value
				data[id] = value
			}
		} else if (null!=p) {
			var childs = $(p).find('input')
			for(i=0; i< childs.length; i++) {
				let id = childs[i].id
				let value = childs[i].value
				if ($(childs[i]).attr('type') == 'password') {
					//TODO: need better hiding of passwords than this really
					let v = "1234567890abcdef1234567890abcdef"
					let bv = CryptoJS.enc.Hex.parse(v);
					let key = CryptoJS.PBKDF2(v, bv, { keySize: 128/32, iterations: 100 });
					let encrypted = CryptoJS.AES.encrypt(value, key, { iv: bv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7  })
					data[id] = encrypted.toString()
				} else {
					data[id] = value
				}						
			}
			var ts = $(p).find('table.input')
			for(i=0; i< ts.length; i++) {
				let tbl = ts[i]
				let entries = []
				for(var r=1; r<tbl.rows.length; r++) { //row 0 should contain the table headers
					var row = tbl.rows[r]
					if ($(row).hasClass('table-row-template')) {
						// don't add the template
					} else {
						var e = {}
						for(var c=0; c<row.cells.length; c++) {
							var cell = row.cells[c]
							var th = $(tbl).find('th').eq($(cell).index())
							if(th[0].hasAttribute('id')) {
								var key = th.attr('id')
								var value = $(cell).text()
								e[key] = value
							} else {
								//don't add vlaue if can't get an id/key
							}
						}
						entries.push(e)
					}
				}
				let id = tbl.id
				data[id] = entries
			}
			// input from textareas
			var tas = $(p).find('textarea.input')
			for(i=0; i< tas.length; i++) {
				let ta = tas[i]
				let id = ta.id
				let value = $(ta).val()
				data[id] = value
			}
			//input from select dropdowns
			var selects = $(p).find('select.input')
			for(i=0; i< selects.length; i++) {
				let select = selects[i]
				let id = select.id
				let value = $('#'+id+' option:selected').val()
				data[id] = value
			}
			
			//check for input from editors
			let eds = $(p).find('.editor.input')
			for(i=0; i< eds.length; i++) {
				let ed = eds[i]
				let id = ed.id
				if (id in this.editors) {
					let value = this.editors[id].getText()
					data[id] = value
				}
			}
		}
		return data
	}
	
	Dynamic.prototype.fetchDialogId = function(el) {
		let dialog = el.closest('dialog')
		if (null==dialog) {
			return null
		} else {
			let dialogId = $(dialog).attr('id')
			return dialogId
		}
		
	}
	
	Dynamic.prototype.requestRecieveEvent = function(elementId, eventType, eventChannelId) {
		var dyn = this
		//first try element identity
		var el = $('#'+elementId)
		//if not found, try find an element with attribute data-ref=elementId
		// used in cases where multiple elements take the same ref/id (e.g. table rows)
		if (el.length == 0) {
			el = $('[data-ref='+elementId+']')
		}
		//if not found, try a class name
		//depricate this...use data-ref instead
		if (el.length == 0) {
			el = $('.'+elementId)
		}
		//if still not found then error
		if (el.length == 0) {
			console.log('Error: cannot find element with id or class ' + elementId)
		}
		var dy = this
		var sceneId = this.sceneId
		$(el).on(eventType, function(event) {
			try {
				event.stopPropagation()
				let data = dy.fetchEventData(this)
				let dialogId = dy.fetchDialogId(this)
				var outData = {stageId: dyn.stageId, sceneId: dyn.sceneId, dialogId:dialogId, elementId:elementId, eventType:eventType, eventData:data}
				console.log("event: "+outData)
				dyn.commsSend(eventChannelId, outData)
			} catch (err) {
				console.log("Error: "+err.message)
			}
		})
	}
	
	Dynamic.prototype.switchToScene = function(stageId, sceneId,sceneArguments) {
		var myLocation = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port;
		var prootPath = this.rootPath + '/'
		var pstageId = stageId + '/'
		if (stageId.length==0) {
		   pstageId = ''
		}
		var psceneId = sceneId + '/'
		if (sceneId.length==0) {
		   psceneId = ''
		}
		var newRef = myLocation + prootPath + pstageId + psceneId
		var refArgs = ''
			if (null!=sceneArguments && !jQuery.isEmptyObject(sceneArguments)) {
				refArgs ='?'
				jQuery.each(sceneArguments, function(key,val) {
					refArgs += key + '=' + val +'&'
				});
				//remove the last '&'
				refArgs = refArgs.substr(0,refArgs.length-1)
			}
		window.location.href = newRef + refArgs
	}
	
	Dynamic.prototype.setTitle = function(value) {
		document.title=value
	}
	
	Dynamic.prototype.setText = function(id, value) {
		var el = $('#'+id)
		if (el.is('input') || el.is('textarea')) {
			el.val(value)
		} else {
			el.text(value)	
		}
	}
	
	Dynamic.prototype.set = function(id, property, value) {
		var el = $('#'+id)
		el.attr(property, value)
	}
	
	Dynamic.prototype.showDialog = function(parentId, dialogId, content) {
		let dialog = document.createElement('dialog')
		if (null!=dialogId) {
			dialog.id = dialogId
		}

		document.body.appendChild(dialog)
		if (null != content) {
			dialog.insertAdjacentHTML('beforeend', content)
		}

	}
	
	Dynamic.prototype.addElement = function(parentId, newElementId, type, attributes, content) {
		var child = document.createElement(type)
		
		if (null!=newElementId) { child.id = newElementId }
		var parent = document.getElementById(parentId)
		if (null == parent) {
			console.log('Error: cannot find parent element with id ' + parentId)
		} else {
			parent.appendChild(child)
	
			if (null != attributes) {
				for ( var key in attributes) {
					if (attributes.hasOwnProperty(key)) {
						child.setAttribute(key, attributes[key])
					}
				}
			}
	
			if (null != content) {
				child.insertAdjacentHTML('beforeend', content)
			}
			return 'ok'
		}
	}
	
	Dynamic.prototype.elementClear = function(elementId) {
		var el = $('#'+elementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + elementId)
		} else {
			$(el).empty()
			return 'ok'
		}
	}
	
	Dynamic.prototype.elementDisable = function(elementId, value) {
		var el = $('#'+elementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + elementId)
		} else {
			$(el).prop('disabled', value)
			return 'ok'
		}
	}
	
	Dynamic.prototype.removeElement = function(elementId) {
		var el = $('#'+elementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + elementId)
		} else {
			el.remove()
			return 'ok'
		}
	}
	
	Dynamic.prototype.tableAddColumn = function(tableId, colHeaderContent, rowTemplateCellContent, existingRowCellContent) {
		require(["Table"],function(Table) {
			let t = new Table(tableId)
			t.addColumn(colHeaderContent, rowTemplateCellContent, existingRowCellContent);
		})
	}
	
	Dynamic.prototype.tableAppendRow = function(tableId, rowData) {
		require(["Table"],function(Table) {
			let t = new Table(tableId)
			t.appendRow(rowData);
		});
		
//		var table = $('#'+tableId)
//		if (table.length == 0) {
//			console.log('Error: cannot find table element with id ' + tableId)
//		} else {
//			var rowTemplate = $(table).find('tr.table-row-template')
//			if (rowTemplate.length ==0) {
//				console.log('Error: table does not define a table-row-template ' + tableId)
//			} else {
//				let rowTemplateHtml = $(rowTemplate)[0].outerHTML
//				let row = rowData
//				let tpl = eval('`'+rowTemplateHtml+'`');
//				let tbody = $(table).find('tbody')
//				let tr = $(tpl)
//				$(tr).removeClass('table-row-template')
//				$(tbody).append(tr)
//				rowTemplate[0].cloneEventsTo(tr[0])
//			}
//		}
	}
	
	Dynamic.prototype.tableRemoveRow = function(tableId, rowId) {
		require(["Table"],function(Table) {
			let t = new Table(tableId)
			t.removeRow(rowId);
		});
//		var table = $('#'+tableId)
//		if (table.length == 0) {
//			console.log('Error: cannot find table element with id ' + tableId)
//		} else {
//			$(table).find('#'+rowId).remove()
//		}
	}
	
	Dynamic.prototype.tableClearAllRows = function(tableId) {
		require(["Table"],function(Table) {
			let t = new Table(tableId)
			t.clearAllRows();
		});
//		var table = $('#'+tableId)
//		if (table.length == 0) {
//			console.log('Error: cannot find table element with id ' + tableId)
//		} else {
//			$(table).find('tbody').find('tr').not('.table-row-template').remove()
//		}
	}
	
	
	Dynamic.prototype.createEditor = function(parentId, languageId, initialContent) {
		let dynamic = this
		if ($('#'+parentId).length == 0) {
			console.log('Error: cannot find element with id ' + parentId)
		} else {
			require(["Editor"],function(Editor) {
				let ed = new Editor(parentId, languageId, initialContent)
				dynamic.editors[parentId] = ed
			})
		}
	}
	
	Dynamic.prototype.updateParseTree = function(id, parseTree) {
		var dynamic = this
		
		let ed = this.editors[id]
		if (null!=d) {
			ed.updateParseTree(parseTree)
		} else {
			console.log('Cannot find Editor for id = '+parentId)
		}
		
	}
		
	
	Dynamic.prototype.addChart = function(parentId, chartId, chartType, chartData, chartOptions) {
		require(["chartjs"],function(){
			let parent = document.getElementById(parentId)
			let width = $(parent).width()
			let height = $(parent).height()
			$(parent).append("<canvas id='"+chartId+"' width='"+width+"' height='"+height+"'></canvas>")
			let chartContext = document.getElementById(chartId).getContext("2d")
			let chart = new Chart(chartContext, {type:chartType, data: chartData, options:chartOptions})
			//chart[chartType](chartData, chartOptions)
		})
	}
	
	Dynamic.prototype.createDiagram = function(parentId, data) {
		var dynamic = this
		if ($('#'+parentId).length == 0) {
			console.log('Error: cannot find element with id ' + parentId)
		} else {		
			require(["Diagram"],function(Diagram) {
				let d = new Diagram(parentId, data)
				dynamic.diagrams[parentId] = d
			})
		}
	}

	Dynamic.prototype.updateDiagram = function(parentId, data) {
		var dynamic = this
		
		let d = this.diagrams[parentId]
		if (null!=d) {
			d.update(data)
		} else {
			console.log('Cannot find Diagram for id = '+parentId)
		}
	}
	
	Dynamic.prototype.commsSend = function(name, data) {
		this.serverComms.send(name,data)
	}
	
	Dynamic.prototype.initComms = function(prefix) {
		var dynamic = this
		let commsPath = prefix+'sockjs'
		console.log('server comms path = '+commsPath)
		this.serverComms = new ServerComms(commsPath, function() {
			console.log('server comms open')
			var sceneArgs = {}
			window.location.search.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(str,key,value) {
			    sceneArgs[key] = value;
			})
			var outData = {stageId: dynamic.stageId, sceneId: dynamic.sceneId, eventType: 'IGuiNotification.notifySceneLoaded', elementId:'', eventData:{sceneArgs:sceneArgs} }
			dynamic.commsSend('IGuiNotification.notifyEventOccured', outData)		
		})
		this.serverComms.registerHandler('Gui.set', function(args) {
			dynamic.set(args.id, args.property, args.value)
		})
		this.serverComms.registerHandler('Gui.setTitle', function(args) {
			dynamic.setTitle(args.value)
		})
		this.serverComms.registerHandler('Gui.setText', function(args) {
			dynamic.setText(args.id, args.value)
		})
		this.serverComms.registerHandler('Gui.addElement', function(args) {
			dynamic.addElement(args.parentId, args.newElementId, args.type, args.attributes, args.content)
		})
		this.serverComms.registerHandler('Gui.removeElement', function(args) {
			dynamic.removeElement(args.elementId)
		})
		this.serverComms.registerHandler('Gui.elementClear', function(args) {
			dynamic.elementClear(args.elementId)
		})
		this.serverComms.registerHandler('Gui.elementDisable', function(args) {
			dynamic.elementDisable(args.elementId, args.value)
		})
		this.serverComms.registerHandler('Gui.requestRecieveEvent', function(args) {
			console.log("requestRecieveEvent "+JSON.stringify(args))
			dynamic.requestRecieveEvent(args.elementId, args.eventType, 'IGuiNotification.notifyEventOccured')
		})
		this.serverComms.registerHandler('Gui.switchToScene', function(args) {
			console.log("switchToScene "+JSON.stringify(args))
			dynamic.switchToScene(args.stageId, args.sceneId, args.sceneArguments)
		})
		this.serverComms.registerHandler('Gui.showDialog', function(args) {
			console.log("showDialog "+JSON.stringify(args))
			dynamic.showDialog(args.parentId, args.dialogId, args.content)
		})
		
		//Tables
		this.serverComms.registerHandler('Table.addColumn', function(args) {
			console.log("Table.addColumn "+JSON.stringify(args))
			dynamic.tableAddColumn(args.tableId, args.colHeaderContent, args.rowTemplateCellContent, args.existingRowCellContent)
		})
		this.serverComms.registerHandler('Table.appendRow', function(args) {
			console.log("Table.appendRow "+JSON.stringify(args))
			dynamic.tableAppendRow(args.tableId, args.rowData)
		})
		this.serverComms.registerHandler('Table.removeRow', function(args) {
			console.log("Table.removeRow "+JSON.stringify(args))
			dynamic.tableRemoveRow(args.tableId, args.rowId)
		})
		this.serverComms.registerHandler('Table.clearAllRows', function(args) {
			console.log("Table.clearAllRows "+JSON.stringify(args))
			dynamic.tableClearAllRows(args.tableId)
		})
		
		//Editors
		this.serverComms.registerHandler('Editor.addEditor', function(args) {
			console.log("Editor.addEditor "+JSON.stringify(args))
			dynamic.createEditor(args.parentId, args.languageId, args.initialContent)
		})
		this.serverComms.registerHandler('Editor.updateParseTree', function(args) {
			console.log("Editor.updateParseTree "+JSON.stringify(args))
			dynamic.updateParseTree(args.id, args.parseTree)
		})
		
		//Charts
		this.serverComms.registerHandler('Gui.addChart', function(args) {
			console.log("addChart "+JSON.stringify(args))
			dynamic.addChart(args.parentId, args.chartId, args.chartType, args.chartData, args.chartOptions)
		})
		this.serverComms.registerHandler('Gui.createDiagram', function(args) {
			console.log("createDiagram "+JSON.stringify(args))
			dynamic.createDiagram(args.parentId, args.data)
		})
		this.serverComms.registerHandler('Gui.updateDiagram', function(args) {
			console.log("updateDiagram "+JSON.stringify(args))
			dynamic.updateDiagram(args.parentId, args.data)
		})
		
		//2d Canvas
		this.serverComms.registerHandler('Canvas.addChild', function(args) { //probably do't need this, can use addElement from Gui
			console.log("Canvas.addChild "+JSON.stringify(args))
			//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
		})
		this.serverComms.registerHandler('Canvas.relocate', function(args) {
			console.log("Canvas.relocate "+JSON.stringify(args))
			//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
		})
		this.serverComms.registerHandler('Canvas.resize', function(args) {
			console.log("Canvas.resize "+JSON.stringify(args))
			//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
		})
		this.serverComms.registerHandler('Canvas.transform', function(args) {
			console.log("Canvas.transform "+JSON.stringify(args))
			//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
		})
	}

	return Dynamic

})