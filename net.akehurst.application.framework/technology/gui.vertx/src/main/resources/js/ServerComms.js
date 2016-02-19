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
function ServerComms(path, onopen) {
	this.socket = new SockJS(path)
	this.handlers = new Map
	this.socket.onopen = onopen
	this.socket.onmessage = this.receive.bind(this)
}

ServerComms.prototype.registerHandler = function(channelId, handler) {
	this.handlers.set(channelId, handler)
}

ServerComms.prototype.send = function(channelId, data) {
	var msg = {channelId:channelId, data:data}
	var packet = JSON.stringify(msg)
	this.socket.send(packet)
}

ServerComms.prototype.receive = function(e) {
	var msg = JSON.parse(e.data)
	var channelId = msg.channelId
	var data = msg.data
	var h = this.handlers.get(channelId)
	if (null==h) {
		console.log('no handler for '+channelId)
	} else {
		h(data)
	}
}