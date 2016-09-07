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

if (typeof stageId === 'undefined' || null===stageId) { //may have an empty ("") stage id
	alert("stageId must be given a value")
}

var expectedStart = rootPath+"/"+stageId
//if (rootPath === 'undefined' || null===rootPath || rootPath === '/' || rootPath.length === 0) {
//  expectedStart = stageId
//}
var path = window.location.pathname

if (!path.startsWith(expectedStart)) {
	alert("'rootPath +'/'+stageId' must have a value that matches the start of the url path, currently expectedStart = '"+expectedStart+"'")
}

var sceneId = path.substring(expectedStart.length+1, path.length-1); //pick sceneId out of path and loose the leading and trailing '/'
if (path == '/') {
  sceneId = "" //handle special case
}

//var end = sceneId.lastIndexOf('/')
//var sceneIdRoot = sceneId.substring(0,end)
//var stageId = sceneIdRoot
console.log("sceneId="+sceneId)

var eventbus = null
var dynamic = null


$(document).ready(function() {
	dynamic = new Dynamic(stageId, sceneId)
})

	Element.prototype.cloneEventsTo = function(clone) {

			let events = jQuery._data(this,'events')
			for(let type in events) {
				$.each(events[type], function(ix, h) {
					jQuery.event.add(clone, type, h.handler, h.data)
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
					jQuery.event.add(c, type, h.handler, h.data)
				})
			}
		} //for
	}