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
 
 define(["sockjs"],function(SockJS) {
  
	function ServerComms(path, onopen) {
		this.socket = new SockJS(path)
		this.handlers = new Map
		this.synchronousCalls = []
		this.socket.onopen = onopen
		this.socket.onmessage = this.receive.bind(this)
	}
	
	ServerComms.prototype.registerHandler = function(channelId, handler) {
		this.handlers.set(channelId, handler)
	}
	
	// Asynchronous send message
	ServerComms.prototype.send = function(channelId, data) {
		let msg = {channelId:channelId, data:data}
		let packet = JSON.stringify(msg)
		this.socket.send(packet)
	}
	
	// Asynchronous receive message
	ServerComms.prototype.receive = function(e) {
		let msg = JSON.parse(e.data)
		let channelId = msg.channelId
		let data = msg.data
		
		 
		if (channelId in this.synchronousCalls) {
		    //if we are waiting for a response on this channel, call the response function
			this.synchronousCalls[channelId](data)
			delete this.synchronousCalls[channelId]
		} else {
		    // else look for a registered handler and call that
			let h = this.handlers.get(channelId)
			if (null==h) {
				console.log('no handler for '+channelId)
			} else {
				//add the call to the javascript event loop,
				//to ensure things are executed in order
				setTimeout(function() {
					console.log(channelId+' '+JSON.stringify(data))
					h(data)
				},0)
			}
		}
	}

	ServerComms.prototype.call = function(channelId, e, callback) {
		let self = this
		let promise = new Promise((resolve,reject)=>{
			self.synchronousCalls[channelId] = resolve
			self.send(channelId, e)
		})
		
		return promise
	}

	return ServerComms

})