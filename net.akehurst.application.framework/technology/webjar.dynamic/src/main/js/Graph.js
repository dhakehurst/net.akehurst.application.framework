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
	"jquery",
	"cytoscape",
	"cytoscape-dagre",
	"dagre"
], function($, cytoscape, cydagre, dagre) {

	function Graph(elementId, initData) {
		cydagre( cytoscape, dagre )
		this.cy = {}
		this.create(elementId, initData)

	}

	Graph.prototype.create = function(elementId, data) {
		let self = this
		try {
		    let el = document.getElementById(elementId)
		    $(el).empty()
		    let containerId = elementId+'-graph-container'
		    let container = $("<div id='"+containerId+"' style='width:100%;height:100%;'></div>").appendTo(el)
		    let initCy = {
					container: document.getElementById(containerId),
					elements : data.elements,
					style : data.style,
					layout: data.layout
				}
		    initCy = $.extend(initCy, data.options)
	        this.cy = cytoscape(initCy)
			
			this.cy.on('click', 'node', {}, function(evt) {
				let nodeId = evt.cyTarget.id()
				let data = dynamic.fetchEventData(el)
				data['nodeId'] = nodeId
				let outData = {stageId: dynamic.stageId, sceneId: dynamic.sceneId, elementId:elementId, eventType:'click', eventData:data}
				dynamic.commsSend('IGuiNotification.notifyEventOccured', outData)
			})
			
			return "ok"
		} catch (err) {
			console.log("Error: "+err.message)
			console.log("Error: "+err.stack)
			return "error"
		}
	}
	
	Graph.prototype.update = function(data) {
		
	}

	return Graph
})