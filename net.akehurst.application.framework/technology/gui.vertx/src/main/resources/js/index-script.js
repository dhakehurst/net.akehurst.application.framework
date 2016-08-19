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

if (typeof stageId === 'undefined' || null===stageId || stageId.length === 0) {
	alert("stageId must be given a value")
}

var expectedStart = rootPath+"/"+stageId
if (rootPath === 'undefined' || null===rootPath || rootPath === '/' || rootPath.length === 0) {
  expectedStart = stageId
}
var path = window.location.pathname

if (!path.startsWith(expectedStart)) {
	alert("'rootPath +'/'+stageId' must have a value that matches the start of the url path, currently expectedStart = '"+expectedStart+"'")
}

var sceneId = path.substring(expectedStart.length);
var sceneArgs = window.location.search
//var end = sceneId.lastIndexOf('/')
//var sceneIdRoot = sceneId.substring(0,end)
//var stageId = sceneIdRoot
console.log("sceneId="+sceneId)

var eventbus = null
var dynamic = null


$(document).ready(function() {
	dynamic = new Dynamic(stageId, sceneId)

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