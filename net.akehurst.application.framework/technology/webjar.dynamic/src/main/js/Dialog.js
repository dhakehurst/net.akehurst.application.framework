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
 
 define([
	"jquery"
], function($) {

	function Dialog(dialogId,parentSelector) {
	    if (null==parentSelector) {
	    	this.parentElement = document.body
	    } else {
			this.parentElement = $(parentSelector)[0]
		}
		this.dialogId = dialogId
		this.dialogElement = document.getElementById(dialogId)
	}

	Dialog.prototype.create = function(dialogContent) {
		if(null==this.dialogElement) {
			let container = document.createElement('div')
			container.id = this.dialogId+'_backdrop'
			$(container).addClass('dialog-backdrop')
			this.parentElement.appendChild(container)
            let dialog = document.createElement('dialog')
            dialog.id = this.dialogId
            container.appendChild(dialog)
            $(this.dialogElement).attr('open','false')
            dialog.insertAdjacentHTML('beforeend',dialogContent)
        } else {
        	console.log('Error: already created this dialog element')
        }
	}
	
	Dialog.prototype.open = function() {
		if(null==this.dialogElement) {
			console.log('Error: must create the dialog before calling open')
        } else {
        	//this.dialogElement.showModal()
        	$(this.dialogElement).attr('open','true')
        }
	}
	
	Dialog.prototype.close = function() {
		if(null==this.dialogElement) {
			console.log('Error: must create the dialog before calling close')
        } else {
        	//this.dialogElement.close()
        	$(this.dialogElement).removeAttr('open')
        	this.delete()
        }
	}
	
	Dialog.prototype.delete = function() {
		if(null==this.dialogElement) {
			console.log('Error: must create the dialog before calling close')
        } else {
        	$('#'+this.dialogId+'_backdrop').remove()
        }
	}
	
	return Dialog
})