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
function Dynamic() {

}

Dynamic.prototype.requestRecieveEvent = function(elementId, eventType, eventChannelId) {
	var el = document.getElementById(elementId)
	el.addEventListener(eventType, function() {
		var data = {}
		if (null!=this.form) {
			for(i=0; i< this.form.length; i++) {
				var id = this.form[i].id
				var value = this.form[i].value
				data[id] = value
			}
		} else if (this.parentElement.tagName=='FIELDSET') {
			var childs = this.parentElement.children
			for(i=0; i< childs.length; i++) {
				if (childs[i].tagName=='INPUT') {
					var id = childs[i].id
					var value = childs[i].value
					data[id] = value							
				}
			}
		}
		var outData = {elementId:el.id, eventType:eventType, eventData:data}
		serverComms.send(eventChannelId, outData)
	})
}

Dynamic.prototype.switchToScene = function(stageId, sceneId) {
	var myLocation = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port;
	window.location.href = myLocation + stageId+sceneId+"/"
}

Dynamic.prototype.setTitle = function(value) {
	document.title=value
}

Dynamic.prototype.setText = function(id, value) {
	$('#'+id).text(value)
}

Dynamic.prototype.addElement = function(parentId, newElementId, type, attributes, content) {
	var child = document.createElement(type)
	child.id = newElementId
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