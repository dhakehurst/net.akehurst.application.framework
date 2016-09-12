// cache event listeners, used by table methods so that dynamicly added rows can 
// reuse the event handlers
(function() {

	Element.prototype.cloneEventsTo = function(clone) {
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

})();