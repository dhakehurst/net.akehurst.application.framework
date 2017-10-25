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
], function($) {

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
		    this.grid=parent
		    $(this.grid).css('display','grid')
		    $(this.grid).css('grid-template-columns','1fr 1fr 1fr')

			this.itemTemplate = $(this.grid).parent().find('.grid-item-template')
		    
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
				$(el).addClass('grid-item')
				let itemEl = el //$("<div></div>")
				//$(el).appendTo($(itemEl))
				if (null == x || null==y || null==w || null==h) {
					$(this.grid).append(itemEl)
				} else {
					$(this.grid).append(itemEl)
					$(itemEl).css('grid-row-start',x)
					$(itemEl).css('grid-column-start',y)
					$(itemEl).css('grid-row-end',x+w)
					$(itemEl).css('grid-column-end',y+h)
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