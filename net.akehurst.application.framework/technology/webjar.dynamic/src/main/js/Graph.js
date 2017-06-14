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

	function Graph(parentId, initData) {
		cydagre( cytoscape, dagre )
		this.cy = {}
		this.create(parentId, initData)

	}

	Graph.prototype.create = function(parentId, data) {
		let self = this
		try {
		    let parent = document.getElementById(parentId)
		    let containerId = parentId+'-graph-container'
		    let container = $("<div id='"+containerId+"' style='width:100%;height:100%;'></div>").appendTo(parent)
	        this.cy = cytoscape({
				container: document.getElementById(containerId),
				elements : data.elements,
				style : data.style,
				layout: data.layout,
				//to increase performnce
				hideEdgesOnViewport: true,
				hideLabelsOnViewport: true,
				textureOnViewport: true
				
			})
			
			this.cy.on('click', 'node', {}, function(evt) {
				var nodeId = evt.cyTarget.id()
				var data = dynamic.fetchEventData(parent)
				data['nodeId'] = nodeId
				var outData = {stageId: dynamic.stageId, sceneId: dynamic.sceneId, elementId:parentId, eventType:'click', eventData:data}
				dynamic.commsSend('IGuiNotification.notifyEventOccured', outData)
			})
			
			return "ok"
		} catch (err) {
			console.log("Error: "+err.message)
			return "error"
		}
	}
	
	Graph.prototype.update = function(data) {
		
	}

	
	//	require(["cytoscape", "cytoscape-dagre", "dagre"],function(cytoscape, cydagre, dagre) {
	//	cydagre( cytoscape, dagre )
	//	var parent = document.getElementById(parentId)
	//	var cy = cytoscape({
	//		container : parent,
	//		elements : data.elements,
	//		style : data.style,
	//		layout: data.layout
	//	})
	//
	//	cy.on('tap', 'node', {}, function(evt) {
	//		var nodeId = evt.cyTarget.id()
	//		var data = dynamic.fetchEventData(parent)
	//		data['nodeId'] = nodeId
	//		var outData = {stageId: dynamic.stageId, sceneId: dynamic.sceneId, elementId:parentId, eventType:'tap', eventData:data}
	//		dynamic.commsSend('IGuiNotification.notifyEventOccured', outData)
	//	})
	//})
	return Graph
})