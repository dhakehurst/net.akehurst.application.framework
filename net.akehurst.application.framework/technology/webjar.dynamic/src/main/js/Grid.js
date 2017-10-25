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
	"jquery.ui",
	"gridstack",
	"gridstack.jqueryui",
	"css!gridstack"
], function($, jqui, gridstack) {

	function Grid(parentId, options) {
		this.gridId = parentId
		this.grid = {}
		this.itemTemplate = {}
		this.create(options)
	}

	Grid.prototype.create = function(options) {
		let self = this
		try {
		    let parent = $('#'+this.gridId)
		    
		    this.grid=$("<div class='grid-stack'></div>").appendTo($(parent))
		    $(this.grid).gridstack(options);
			this.itemTemplate = $(this.grid).parent().find('.grid-item-template')
		    $(this.grid).on('change', function(event, items){
		    		event.stopPropagation()
		    		let changedItems = []
		    		for (let i = 0, len = items.length; i < len; i++) {
			    		let item = items[i]
		    			let chItem = {
		    				itemId:$(item.el).find('.grid-item').attr('id'),
		    				x:item.x,
		    				y:item.y,
		    				w:item.width,
		    				h:item.height
		    			}
		    			changedItems.push(chItem)
		    		}
		    		let evt = {
		    			items: changedItems
		    		}
		    		$(parent).trigger( "change", evt )
		    })
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}
	
	Grid.prototype.appendItem = function(data, x,y,w,h) {
		try {
			if (this.itemTemplate.length ==0) {
				console.log('Error: grid does not define a grid-item-template ' + this.gridId)
			} else {
				let itemTemplateHtml = $(this.itemTemplate).get(0).outerHTML
				let item = data
				let elt = eval('`'+itemTemplateHtml+'`');
				let el = $(elt)
				$(el).removeAttr('hidden')
				$(el).removeClass('grid-item-template')
				let itemEl = $("<div></div>")
				let c = $("<div class='grid-stack-item-content'></div>").appendTo($(itemEl))
				$(el).appendTo($(c))
				if (null == x || null==y || null==w || null==h) {
					$(this.grid).data('gridstack').addWidget(itemEl,0,0,1,1,true)
				} else {
					$(this.grid).data('gridstack').addWidget(itemEl,x,y,w,h,false)
				}
			}
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}

	Grid.prototype.removeItem = function(itemId) {
		$(this.grid).removeWidget($('#'+itemId).parent().parent())
	}

	return Grid
})