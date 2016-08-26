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
function Dynamic(stageId, sceneId) {
	this.stageId = stageId
	this.sceneId = sceneId
	this.initComms();
}

Dynamic.prototype.fetchEventData = function(el) {
	var data = {}
	var p = el.closest('fieldset')
	if (null!=this.form) {
		for(i=0; i< this.form.length; i++) {
			var id = this.form[i].id
			var value = this.form[i].value
			data[id] = value
		}
	} else if (null!=p) {
		var childs = $(p).find('input')
		for(i=0; i< childs.length; i++) {
			var id = childs[i].id
			var value = childs[i].value
			data[id] = value							
		}
		var ts = $(p).find('table.input')
		for(i=0; i< ts.length; i++) {
			var tbl = ts[i]
			var entries = []
			for(var r=1; r<tbl.rows.length; r++) { //row 0 should contain the table headers
				var row = tbl.rows[r]
				var e = {}
				for(var c=0; c<row.cells.length; c++) {
					var cell = row.cells[c]
					var th = $(tbl).find('th').eq($(cell).index())
					var key = th.attr('id')
					var value = $(cell).text()
					e[key] = value
				}
				entries.push(e)
			}
			var id = tbl.id
			data[id] = entries
		}
		var tas = $(p).find('textarea.input')
		for(i=0; i< tas.length; i++) {
			var ta = tas[i]
			var id = ta.id
			var value = $(ta).val()
			data[id] = value
		}
	}
	return data
}


Dynamic.prototype.requestRecieveEvent = function(elementId, eventType, eventChannelId) {
	var dyn = this
	var el = $('#'+elementId)
	var dy = this
	var sceneId = this.sceneId
	el.click(function() {
		var data = dy.fetchEventData(el)
		var outData = {stageId: dyn.stageId, sceneId: dyn.sceneId, elementId:this.id, eventType:eventType, eventData:data}
		dyn.commsSend(eventChannelId, outData)
	})
}

Dynamic.prototype.switchToScene = function(stageId, sceneId) {
	var myLocation = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port;
	window.location.href = myLocation + stageId+sceneId
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

Dynamic.prototype.clearElement = function(elementId) {
	var el = $('#'+elementId)
	if (el.length == 0) {
		console.log('Error: cannot find parent element with id ' + elementId)
	} else {
		el.empty()
		return 'ok'
	}
}

Dynamic.prototype.addChart = function(parentId, chartId, width, height, chartType, chartData, chartOptions) {
	//currently uses Chart.js
	var parent = document.getElementById(parentId)
	$(parent).append("<canvas id='"+chartId+"' width='"+width+"' height='"+height+"'></canvas>")
	var chartContext = document.getElementById(chartId).getContext("2d")
	var chart = new Chart(chartContext)
	chart[chartType](chartData, chartOptions)
}

Dynamic.prototype.addDiagram = function(parentId, diagramId, data) {
	var dyn = this
	//currently uses Chart.js
	var parent = document.getElementById(parentId)
	var dy = this
	var cy = cytoscape({
		container : parent,
		elements : data.elements,
		style : data.style
	})

	cy.on('tap', 'node', {}, function(evt) {
		var nodeId = evt.cyTarget.id()
		var data = dy.fetchEventData(parent)
		data['nodeId'] = nodeId
		var outData = {stageId: dyn.stageId, sceneId: dyn.sceneId, elementId:diagramId, eventType:'tap', eventData:data}
		dyn.commsSend('IGuiNotification.notifyEventOccured', outData)
	})
}

Dynamic.prototype.commsSend = function(name, data) {
	this.serverComms.send(name,data)
}

Dynamic.prototype.initComms = function() {
	var dyn = this
	this.serverComms = new ServerComms(stageId+'/sockjs', function() {
		console.log('server comms open')
		var outData = {stageId: dyn.stageId, sceneId: dyn.sceneId, eventType: 'IGuiNotification.notifySceneLoaded', elementId:'', eventData:{sceneArgs:sceneArgs} }
		dyn.commsSend('IGuiNotification.notifyEventOccured', outData)		
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
	this.serverComms.registerHandler('Gui.clearElement', function(args) {
		dynamic.clearElement(args.elementId)
	})
	this.serverComms.registerHandler('Gui.requestRecieveEvent', function(args) {
		console.log("requestRecieveEvent "+JSON.stringify(args))
		dynamic.requestRecieveEvent(args.elementId, args.eventType, 'IGuiNotification.notifyEventOccured')
	})
	this.serverComms.registerHandler('Gui.switchToScene', function(args) {
		console.log("switchToScene "+JSON.stringify(args))
		dynamic.switchToScene(args.stageId, args.sceneId)
	})
	//Charts
	this.serverComms.registerHandler('Gui.addChart', function(args) {
		console.log("addChart "+JSON.stringify(args))
		dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
	})
	this.serverComms.registerHandler('Gui.addDiagram', function(args) {
		console.log("addDiagram "+JSON.stringify(args))
		dynamic.addDiagram(args.parentId, args.diagramId, args.data)
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