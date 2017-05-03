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
	"jointjs",
	"css!jointjs"
], function($, joint) {

	function Diagram(parentId, initData) {
		this.graph = {}
		this.paper = {}
		this.create(parentId)
		this.update(initData)
	}

	Diagram.prototype.create = function(parentId) {
		let self = this
		try {
		    let item = $('#'+parentId)
	        let canvas = $("<div style='height:100%;width:100%;overflow-y:auto;overflow-x:auto;'></div>").appendTo(item)
	
	        this.graph = new joint.dia.Graph()
		    this.paper = new joint.dia.Paper({
	            el: $(canvas),
	            height: $(item).height(),
	            width: $(item).width(),
	            model: self.graph,
	            gridSize: 1
	        });
		    
	        window.onresize = function(event) {
	        	let el = $(item)
	        	self.paper.setDimensions(el.width(), el.height())
	        }
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}
	
	Diagram.prototype.update = function(data) {
		try {
			this.graph.clear()
	        let elements = []
	        
	        for(let i in data.elements) {
	        	let el = data.elements[i]
	        	if (el.group=='nodes') {
	        		let shapeTypeStr = (el.data.shape || 'shapes.basic.Rect')
	        		let shapeType = this.stringToFunction(shapeTypeStr, joint)
	        		let args = el.data
	        		let shape = new shapeType(args)
	        		elements.push(
	    				shape
	        		)
	        	} else if (el.group=='edges') {
	        		let shapeTypeStr = (el.data.shape || 'dia.Link')
	        		let shapeType = this.stringToFunction(shapeTypeStr, joint)
	        		let args = el.data
	        		Object.assign(args, {
	    	            source: { id: el.data.source },
	    	            target: { id: el.data.target }
	    	        })
	        		elements.push(
		        		new shapeType(args)
	        		)
	        	}
	        }
		        
	        this.graph.addCells(elements);
	
		    joint.layout.DirectedGraph.layout(this.graph, {
		    	setLinkVertices: false
		    })
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}

	Diagram.prototype.stringToFunction = function(str, root) {
		try {
		  let arr = str.split(".")
		  let fn = root
		  for (let i = 0, len = arr.length; i < len; i++) {
		    fn = fn[arr[i]]
		  }
		  if (typeof fn !== "function") {
		    throw new Error("function not found")
		  }
		  return  fn
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
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
	return Diagram
})