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
	"crypto.aes",
	"Dialog",
	"Table"
], function($, ServerComms, cryptoPBK, cryptoAES, Dialog, Table) {
 
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
		this.graphs = {}
		this.highlighter = {}
	}
	
	Dynamic.prototype.fetchEventData = function(el) {
	//TODO: not sure if this is quite what we want!
		let d1 = this.fetchPossibleRowId(el,'tr')
		//var d2 = this.fetchEventData1(el,'fieldset') //legacy now replaced with div.event-group (mainly because browser support for fieldset css/flex is broken on some browsers)
		let d3 = this.fetchEventData1(el,'.event-group')
		let data = $.extend({}, d1, d3)
		return data
	}
	Dynamic.prototype.fetchPossibleRowId = function(el, type) {
		let p = el.closest('tr')
		if (null!=p) {
			let id = $(p).attr('id')
			return {afRowId:id}
		} else {
			return {}
		}
	}
	
	Dynamic.prototype.fetchInputData = function(el) {
		let data = {}
		let inTableTemplate = $(el).find('.table-row-template input')
		let childs = $(el).find('input')
		let inputs = $(childs).not(inTableTemplate)
		for(i=0; i< inputs.length; i++) {
			let input = inputs[i]
			let id = input.id
			let value = input.value
			if ($(input).attr('type') == 'password') {
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
		return data
	}
	
	Dynamic.prototype.fetchTableData = function(el) {
		let data = {}
		let inTableTemplate = $(el).find('.table-row-template table.input')
		let childs = $(el).find('table.input')
		let inputs = $(childs).not(inTableTemplate)
		//var ts = $(p).find('table.input')
		for(let i=0; i< inputs.length; i++) {
			let tbl = inputs[i]
			
			let headers = []
			let hrow = tbl.rows[0] //row 0 should contain the table headers
			let unnamedCount = 0;
			for(let c=0; c<hrow.cells.length; c++) { 
				let cell = hrow.cells[c]
				let th = $(tbl).find('th').eq($(cell).index())
				if(th[0].hasAttribute('id')) {
					let key = th.attr('id')
					headers.push(key)
				} else {
					headers.push('unnamed_'+unnamedCount)
					unnamedCount++
				}
			}
			
			let rowsData = []
			for(let r=1; r<tbl.rows.length; r++) { //row 0 should contain the table headers
				let row = tbl.rows[r]
				if ($(row).hasClass('table-row-template')) {
					// don't add the template
				} else {
					var rowData = []
					for(let c=0; c<row.cells.length; c++) {
						let cell = row.cells[c]
						let th = $(tbl).find('th').eq($(cell).index())
						let value = this.fetchData(cell)
						rowData.push(value)
					}
					rowsData.push(rowData)
				}
			}
			let id = tbl.id
			data[id] = { afHeaders:headers, afRows:rowsData }
		}
		return data
	}
	
	Dynamic.prototype.fetchTextAreaData = function(el) {
		let data = {}
		let inTableTemplate = $(el).find('.table-row-template textarea.input')
		let childs = $(el).find('textarea.input')
		let inputs = $(childs).not(inTableTemplate)
		for(i=0; i< inputs.length; i++) {
			let ta = inputs[i]
			let id = ta.id
			let value = $(ta).val()
			data[id] = value
		}
		return data
	}
	
	Dynamic.prototype.fetchSelectData = function(el) {
		let data = {}
		let inTableTemplate = $(el).find('.table-row-template select.input')
		let childs = $(el).find('select.input')
		let inputs = $(childs).not(inTableTemplate)
		//var selects = $(p).find('select.input')
		for(i=0; i< inputs.length; i++) {
			let select = inputs[i]
//			if ($(select).closest('table-row-template')) {
//				//do nothing
//			} else {
				let id = select.id
				let value = $('#'+id+' option:selected').val()
				data[id] = value					
//			}
		}
		return data
	}
	
	Dynamic.prototype.fetchEditorData = function(el) {
		let data = {}
		//handle textarea not in a table-row-template
		let inTableTemplate = $(el).find('.table-row-template .editor.input')
		let childs = $(el).find('.editor.input')
		let inputs = $(childs).not(inTableTemplate)
		//let eds = $(p).find('.editor.input')
		for(i=0; i< inputs.length; i++) {
			let ed = inputs[i]
			let id = ed.id
			if (id in this.editors) {
				let value = this.editors[id].getText()
				data[id] = value
			}
		}
		return data
	}
	
	Dynamic.prototype.fetchData = function(el) {
		let d1 = this.fetchInputData(el)
		let d2 = this.fetchTextAreaData(el)
		let d3 = this.fetchSelectData(el)
		let d4 = this.fetchEditorData(el)
		let d5 = this.fetchTableData(el)
		let data = $.extend({}, d1,d2,d3,d4,d5)
		return data
	}
	
	Dynamic.prototype.fetchEventData1 = function(el, cls) {
		let data = {}
		let ancestorEl = el.closest(cls)
		if (null!=this.form) {
			//TODO: handle forms!
			for(i=0; i< this.form.length; i++) {
				let id = this.form[i].id
				let value = this.form[i].value
				data[id] = value
			}
			return data
		} else if (null!=ancestorEl) {
			return this.fetchData(ancestorEl)
		}
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
		let dyn = this
		let selector = '#'+elementId +','+'[data-ref='+elementId+']'
		let el = $(selector)
		//first try element identity
//		let el = $('#'+elementId)
		//if not found, try find an element with attribute data-ref=elementId
		// used in cases where multiple elements take the same ref/id (e.g. table rows)
//		if (el.length == 0) {
//			el = $('[data-ref='+elementId+']')
//		}
		//if not found, try a class name
		//depricate this...use data-ref instead
//		if (el.length == 0) {
//			el = $('.'+elementId)
//		}
		//if still not found then error
//		if (el.length == 0) {
//			console.log('Error: cannot find element with id or data-ref equal to ' + elementId)
//		}
		let dy = this
		let sceneId = this.sceneId
		let mappedEventType = eventType
		if ($(el).is("select")) {
			if ("oninput"==eventType) {
				mappedEventType = "change"
			}
		}
		//remove previous event handler
		$(document.body).off(mappedEventType, selector)
		$(document.body).on(mappedEventType, selector, function(event) {
			try {
				event.stopPropagation()
				let data = dy.fetchEventData(event.target)
				let dialogId = dy.fetchDialogId(event.target)
				var outData = {stageId: dyn.stageId, sceneId: dyn.sceneId, dialogId:dialogId, elementId:elementId, eventType:eventType, eventData:data}
				console.log("event: "+JSON.stringify(outData))
				dyn.commsSend(eventChannelId, outData)
			} catch (err) {
				console.log("Error: "+err.message)
			}
		})
	}
	
	Dynamic.prototype.navigateTo = function(location) {
		if (location.startsWith('/')) {
			var myLocation = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port;
			var newRef = myLocation + location
	
			window.location.href = newRef
		} else {
			window.location.href = location
		}
	}
	
	Dynamic.prototype.newWindow = function(location) {
		let newLocation = ''
		if (location.startsWith('/')) {
			var myLocation = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port;
			var newRef = myLocation + location
	
			newLocation = newRef
		} else {
			newLocation = location
		}
		let newWindow = window.open(newLocation)
		if (window.focus) {
			newWindow.focus()
		}
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
	
	Dynamic.prototype.download = function (filename, link) {
		  var element = document.createElement('a');
		  element.setAttribute('href', link);
		  element.setAttribute('download', filename);

		  element.style.display = 'none';
		  document.body.appendChild(element);

		  element.click();

		  document.body.removeChild(element);
	}
	
	Dynamic.prototype.upload = function (filenameElementId, uploadLink) {
		var el = $('#'+filenameElementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + filenameElementId)
		} else {
			let file = el[0].files[0]
			let data = new FormData();
			data.append('file', file);
			$.ajax({
				url: uploadLink,
				type: 'POST',
				data : data,
				processData: false,
				contentType: false,
				error : function(data) {
					console.log(data)
					alert(data)
				}
			})
		}
	}
	
	
	Dynamic.prototype.setTitle = function(value) {
		document.title=value
	}
	
	Dynamic.prototype.setText = function(id, value) {
		var el = $('#'+id)
		if (el.is('input') || el.is('textarea') || el.is('select')) {
			el.val(value)
		} else {
			el.text(value)	
		}
	}
	
	Dynamic.prototype.set = function(id, property, value) {
		var el = $('#'+id)
		el.attr(property, value)
	}
	
	Dynamic.prototype.dialogCreate = function(dialogId, content) {
		let dialog = new Dialog(dialogId)
		dialog.create(content)
	}
	
	Dynamic.prototype.dialogOpen = function(dialogId) {
		let dialog = new Dialog(dialogId)
		dialog.open()
	}
	
	Dynamic.prototype.dialogClose = function(dialogId) {
		let dialog = new Dialog(dialogId)
		dialog.close()
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
	
	Dynamic.prototype.elementSetDisabled = function(elementId, value) {
		var el = $('#'+elementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + elementId)
		} else {
			$(el).prop('disabled', value)
			return 'ok'
		}
	}
	
	Dynamic.prototype.elementSetLoading = function(elementId, value) {
		var el = $('#'+elementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + elementId)
		} else {
			let loadingId = elementId+'-loading'
			if (value) {
				$(el).addClass('loading')
				$(el).prop('disabled', true)
				$("<div id='"+loadingId+"' class='loading-spinner-container'><div class='loading-spinner'></div></div>").prependTo($(el))
			} else {
				 $('#'+loadingId).remove()
				 $(el).prop('disabled', false)
				 $(el).removeClass('loading')
			}
			return 'ok'
		}
	}
	
	Dynamic.prototype.elementAddClass = function(elementId, className) {
		var el = $('#'+elementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + elementId)
		} else {
			$(el).addClass(className)
			return 'ok'
		}
	}
	
	Dynamic.prototype.elementRemoveClass = function(elementId, className) {
		var el = $('#'+elementId)
		if (el.length == 0) {
			console.log('Error: cannot find element with id ' + elementId)
		} else {
			$(el).removeClass(className)
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

			let t = new Table(tableId)
			t.addColumn(colHeaderContent, rowTemplateCellContent, existingRowCellContent);

	}
	
	Dynamic.prototype.tableClearAllColumnHeaders = function(tableId) {

			let t = new Table(tableId)
			t.clearAllColumnHeaders();

	}
	
	Dynamic.prototype.tableAppendRow = function(tableId, rowData) {

			let t = new Table(tableId)
			t.appendRow(rowData);

	}
	
	Dynamic.prototype.tableRemoveRow = function(tableId, rowId) {

			let t = new Table(tableId)
			t.removeRow(rowId);

	}
	
	Dynamic.prototype.tableClearAllRows = function(tableId) {

			let t = new Table(tableId)
			t.clearAllRows();

	}
	
	
	Dynamic.prototype.createEditor = function(parentId, languageId, initialContent) {
		let dynamic = this
		if ($('#'+parentId).length == 0) {
			console.log('Error: cannot find element with id ' + parentId)
		} else {
			require(["Editor"],function(Editor) {
				let ed = new Editor(parentId, languageId, initialContent, dynamic.serverComms)
				dynamic.editors[parentId] = ed
			})
		}
	}
	
	Dynamic.prototype.updateParseTree = function(id, parseTree) {
		var dynamic = this
		
		let ed = this.editors[id]
		if (null!=ed) {
			ed.updateParseTree(parseTree)
		} else {
			console.log('Cannot find Editor for id = '+id)
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
	
	Dynamic.prototype.graphCreate = function(parentId, data) {
		var dynamic = this
		if ($('#'+parentId).length == 0) {
			console.log('Error: cannot find element with id ' + parentId)
		} else {		
			require(["Graph"],function(Graph) {
				let d = new Graph(parentId, data)
				dynamic.graphs[parentId] = d
			})
		}
	}

	Dynamic.prototype.graphUpdate = function(parentId, data) {
		var dynamic = this
		let d = this.graphs[parentId]
		if (null!=d) {
			d.update(data)
		} else {
			console.log('Cannot find Graph for id = '+parentId)
		}
	}
	
	Dynamic.prototype.commsSend = function(name, data) {
		this.serverComms.send(name,data)
	}
	
	Dynamic.prototype.initComms = function(prefix) {
		let dynamic = this
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
		this.serverComms.registerHandler('Gui.setTitle', function(args) {
			dynamic.setTitle(args.value)
		})
		this.serverComms.registerHandler('Gui.setText', function(args) {
			dynamic.setText(args.id, args.value)
		})
		
		this.serverComms.registerHandler('Element.setProperty', function(args) {
			dynamic.set(args.id, args.property, args.value)
		})
		this.serverComms.registerHandler('Element.add', function(args) {
			dynamic.addElement(args.parentId, args.newElementId, args.type, args.attributes, args.content)
		})
		this.serverComms.registerHandler('Element.remove', function(args) {
			dynamic.removeElement(args.id)
		})
		this.serverComms.registerHandler('Element.clear', function(args) {
			dynamic.elementClear(args.id)
		})
		this.serverComms.registerHandler('Element.setDisabled', function(args) {
			dynamic.elementSetDisabled(args.id, args.value)
		})
		this.serverComms.registerHandler('Element.setLoading', function(args) {
			dynamic.elementSetLoading(args.id, args.value)
		})
		this.serverComms.registerHandler('Element.addClass', function(args) {
			dynamic.elementAddClass(args.id, args.className)
		})
		this.serverComms.registerHandler('Element.removeClass', function(args) {
			dynamic.elementRemoveClass(args.id, args.className)
		})
		
		this.serverComms.registerHandler('Gui.download', function(args) {
			dynamic.download(args.filename, args.link)
		})
		this.serverComms.registerHandler('Gui.upload', function(args) {
			dynamic.upload(args.filenameElementId, args.uploadLink)
		})
		this.serverComms.registerHandler('Gui.requestRecieveEvent', function(args) {
			dynamic.requestRecieveEvent(args.elementId, args.eventType, 'IGuiNotification.notifyEventOccured')
		})
		this.serverComms.registerHandler('Gui.navigateTo', function(args) {
			dynamic.navigateTo(args.location)
		})
		this.serverComms.registerHandler('Gui.switchToScene', function(args) {
			dynamic.switchToScene(args.stageId, args.sceneId, args.sceneArguments)
		})
		this.serverComms.registerHandler('Gui.newWindow', function(args) {
			dynamic.newWindow(args.location)
		})
		this.serverComms.registerHandler('Dialog.create', function(args) {
			dynamic.dialogCreate(args.dialogId, args.content)
		})
		this.serverComms.registerHandler('Dialog.open', function(args) {
			dynamic.dialogOpen(args.dialogId)
		})
		this.serverComms.registerHandler('Dialog.close', function(args) {
			dynamic.dialogClose(args.dialogId)
		})
		//Tables
		this.serverComms.registerHandler('Table.addColumn', function(args) {
			dynamic.tableAddColumn(args.tableId, args.colHeaderContent, args.rowTemplateCellContent, args.existingRowCellContent)
		})
		this.serverComms.registerHandler('Table.clearAllColumnHeaders', function(args) {
			dynamic.tableClearAllColumnHeaders(args.tableId)
		})
		this.serverComms.registerHandler('Table.appendRow', function(args) {
			dynamic.tableAppendRow(args.tableId, args.rowData)
		})
		this.serverComms.registerHandler('Table.removeRow', function(args) {
			dynamic.tableRemoveRow(args.tableId, args.rowId)
		})
		this.serverComms.registerHandler('Table.clearAllRows', function(args) {
			dynamic.tableClearAllRows(args.tableId)
		})
		
		//Editors
		this.serverComms.registerHandler('Editor.addEditor', function(args) {
			dynamic.createEditor(args.parentId, args.languageId, args.initialContent)
		})
		this.serverComms.registerHandler('Editor.updateParseTree', function(args) {
			dynamic.updateParseTree(args.editorId, args.parseTree)
		})
		
		//Charts
		this.serverComms.registerHandler('Gui.addChart', function(args) {
			dynamic.addChart(args.parentId, args.chartId, args.chartType, args.chartData, args.chartOptions)
		})
		
		//Diagram
		this.serverComms.registerHandler('Diagram.create', function(args) {
			dynamic.createDiagram(args.parentId, args.data)
		})
		this.serverComms.registerHandler('Diagram.update', function(args) {
			dynamic.updateDiagram(args.parentId, args.data)
		})
		
		//Diagram
		this.serverComms.registerHandler('Graph.create', function(args) {
			dynamic.graphCreate(args.parentId, args.data)
		})
		this.serverComms.registerHandler('Graph.update', function(args) {
			dynamic.graphUpdate(args.parentId, args.data)
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