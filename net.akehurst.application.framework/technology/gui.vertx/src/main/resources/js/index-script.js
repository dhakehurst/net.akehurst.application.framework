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
"use strict"

var sceneId = window.location.pathname
var sceneArgs = window.location.search
var end = sceneId.lastIndexOf('/')
var sceneIdRoot = sceneId.substring(0,end)
var stageId = sceneIdRoot
console.log("sceneId="+sceneId)

var eventbus = null
var dynamic = null
var serverComms = null;


$(document).ready(function() {
	dynamic = new Dynamic(sceneId)
	serverComms = new ServerComms('/sockjs'+stageId, function() {
		console.log('server comms open')
		var outData = {sceneId: sceneId, eventType: 'IGuiNotification.notifySceneLoaded', elementId:'', eventData:{sceneArgs:sceneArgs} }
		serverComms.send('IGuiNotification.notifyEventOccured', outData)		
	})
	serverComms.registerHandler('Gui.setTitle', function(args) {
		dynamic.setTitle(args.value)
	})
	serverComms.registerHandler('Gui.setText', function(args) {
		dynamic.setText(args.id, args.value)
	})
	serverComms.registerHandler('Gui.addElement', function(args) {
		dynamic.addElement(args.parentId, args.newElementId, args.type, args.attributes, args.content)
	})
	serverComms.registerHandler('Gui.clearElement', function(args) {
		dynamic.clearElement(args.elementId)
	})
	serverComms.registerHandler('Gui.requestRecieveEvent', function(args) {
		console.log("requestRecieveEvent "+JSON.stringify(args))
		dynamic.requestRecieveEvent(args.elementId, args.eventType, 'IGuiNotification.notifyEventOccured')
	})
	serverComms.registerHandler('Gui.switchToScene', function(args) {
		console.log("switchToScene "+JSON.stringify(args))
		dynamic.switchToScene(args.stageId, args.sceneId)
	})
	//Charts
	serverComms.registerHandler('Gui.addChart', function(args) {
		console.log("addChart "+JSON.stringify(args))
		dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
	})
	serverComms.registerHandler('Gui.addDiagram', function(args) {
		console.log("addDiagram "+JSON.stringify(args))
		dynamic.addDiagram(args.parentId, args.diagramId, args.data)
	})
	
	//2d Canvas
	serverComms.registerHandler('Canvas.addChild', function(args) { //probably do't need this, can use addElement from Gui
		console.log("Canvas.addChild "+JSON.stringify(args))
		//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
	})
	serverComms.registerHandler('Canvas.relocate', function(args) {
		console.log("Canvas.relocate "+JSON.stringify(args))
		//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
	})
	serverComms.registerHandler('Canvas.resize', function(args) {
		console.log("Canvas.resize "+JSON.stringify(args))
		//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
	})
	serverComms.registerHandler('Canvas.transform', function(args) {
		console.log("Canvas.transform "+JSON.stringify(args))
		//dynamic.addChart(args.parentId, args.chartId, args.width, args.height, args.chartType, args.chartData, args.chartOptions)
	})
	//eventbus = new EventBus(myLocation + '/eventbus')
//	eventbus = new EventBus('http://localhost:9998' + '/eventbus')
//
//	eventbus.onopen = function() {
//
//		
//		eventbus.registerHandler('Canvas.addChild', function(x, packet) {
//			console.log("addChild "+JSON.stringify(packet))
//			var args = packet.body
//			diagram.addChild( args.child )
//		});
//		
//		eventbus.registerHandler('Canvas.addChildToParent', function(x, packet) {
//			console.log("addChildToParent "+JSON.stringify(packet))
//			var args = packet.body
//			diagram.addChildToParent( args.parentId, args.child )
//		});
//		
//		eventbus.registerHandler('Canvas.relocate', function(x, packet) {
//			console.log("relocate "+JSON.stringify(packet))
//			var args = packet.body
//			diagram.relocate( args.id, args.x, args.y )
//		});
//		
//		eventbus.registerHandler('Canvas.resize', function(x, packet) {
//			console.log("resize "+JSON.stringify(packet))
//			var args = packet.body
//			diagram.resize( args.id, args.width, args.height )
//		});
//		
//		eventbus.registerHandler('Canvas.transform', function(x, packet) {
//			console.log("transform "+JSON.stringify(packet))
//			var args = packet.body
//			diagram.transform( args.id, args.matrix )
//		});
//	
//		eventbus.registerHandler('Canvas.setStartAnchor', function(x, packet) {
//			console.log("setStartAnchor "+JSON.stringify(packet))
//			var args = packet.body
//			diagram.setStartAnchor( args.id, args.anchorId )
//		});
//		
//		eventbus.registerHandler('Canvas.setEndAnchor', function(x, packet) {
//			console.log("setEndAnchor "+JSON.stringify(packet))
//			var args = packet.body
//			diagram.setEndAnchor( args.id, args.anchorId )
//		});
//		// voi addEdge(JsonObject edge)
//	//	eventbus.registerHandler('addEdge', function(x, packet) {
//	//		var edge = packet.body;
//	
//	//	});
//
//
//	}

})