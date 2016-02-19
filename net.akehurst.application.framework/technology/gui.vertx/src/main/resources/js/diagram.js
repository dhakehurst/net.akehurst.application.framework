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
move = function(dx,dy) {
	var t = this.data('origTransform') + (this.data('origTransform') ? "T" : "t") + [dx, dy]
	this.attr({transform: t })
}
start = function(clientX,clientY,ev) {
	this.data('origTransform', this.transform().local )
	var x = clientX //this.node.getBBox().x
	var y = clientY //this.node.getBBox().y	
	this.data('x', x );
	this.data('y', y );
	console.log("onPress "+JSON.stringify({id: ev.currentTarget.id, x: x, y: y }))
	eventbus.publish("onPress", {id: ev.currentTarget.id, x: x, y: y })
}
stop = function(ev) {
	var dx = ev.clientX;
	var dy = ev.clientY;
	var id = this.node.id
	console.log("onRelease "+JSON.stringify({id: id, dx: dx, dy: dy }))
	eventbus.publish("onRelease", {id: id, dx: dx, dy: dy })
}
mouseover = function() {
	this.attr({style:'cursor:grab'})
}
mouseout = function() {
	this.attr({style:''})
}
mousedown = function(ev, x, y) {
	this.attr({style:'cursor:grabbing'})
}
mouseup = function(ev, x, y) {
	this.attr({style:'cursor:grab'})
}

document.onkeypress = function(event) {
	console.log("Keypress: "+event.key)
	eventbus.publish("onKeyboardInput", {characters: event.key })
}

function Diagram() {
	this.paper = Snap("#svg")
	if (null!=this.paper) {
		this.paper.attr({width:800,height:600})
		this.paper.attr({style:'border-color:#aaaaaa;border-style:dotted'})
	}
}

Diagram.prototype.addChild = function(child) {
	if ("svg" == child.type) {
		var g = this.paper.g() //.drag(move,start,stop).mouseover(mouseover).mouseout(mouseout).mousedown(mousedown)
		g.attr('id', child.id)
		var f = Snap.parse(child.svg)
		g.append( f )
//	} else if ("rect" == child.type) {
//		this.paper.g(
//			this.paper.rect(0,0,10,10).attr(child)
//		).drag(move,start,stop).mouseover(mouseover).mouseout(mouseout).mousedown(mousedown)
//	} else if ("circle" == child.type) {
//		this.paper.circle(child.x, child.y, child.r).attr(child.attr)
	} else {
		console.log("Can't create type "+type)
	}
}
Diagram.prototype.addChildToParent = function(parentId, child) {
	if ("svg" == child.type) {
		var parent = Snap.select("#"+parentId);
		var g = this.paper.g().drag(move,start,stop).mouseover(mouseover).mouseout(mouseout).mousedown(mousedown)
		g.appendTo(parent)
		g.attr('id', child.id)
		var f = Snap.parse(child.svg)
		g.append(f)
	} else {
		console.log("Can't create type "+child.type)
	}
}
Diagram.prototype.relocate = function(id, x, y) {
	var el = Snap.select("#"+id);
	transform(el,x,y)
}
Diagram.prototype.resize = function(id, w, h) {
	var el = Snap.select("#"+id);
	var s = el.select('.size')
	s.attr({width:w,height:h})
}
Diagram.prototype.transform = function(id, matrix) {
	var el = Snap.select("#"+id);
	var ot = el.transform().local
	var t =  (ot ? "M" : "m") + matrix
	el.attr({transform: t })
}
Diagram.prototype.setStartAnchor = function(id, anchorId) {
	var line = Snap.select("#"+id+" line");
	var end = Snap.select("#"+anchorId)
	if (null!=end && null!=line) {
		var bb = end.getBBox();
		line.attr({x1: bb.x+bb.width/2, y1: bb.y+bb.height/2 })
	}
}
Diagram.prototype.setEndAnchor = function(id, anchorId) {
	var line = Snap.select("#"+id+" line");
	var end = Snap.select("#"+anchorId)
	if (null!=end && null!=line) {
		var bb = end.getBBox();
		line.attr({x2: bb.x+bb.width/2, y2: bb.y+bb.height/2 })
	}
}
function transform(el, dx, dy) {
	var ot = el.transform().local
	var t =  (ot ? "T" : "t") + [dx, dy]
	el.attr({transform: t })
}