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
	"orion/codeEdit",
	"orion/Deferred",
	"orion/EventTarget",
	"orion/editor/annotations",
	"Highlighter",
	"css!orion/12.0/code_edit/built-codeEdit.css"
], function(
    mCodeEdit,
    Deferred,
    EventTarget,
    mAnnotations,
    mHighlighter
) {

	function Editor(parentId, languageId, initialContent) {
		let self = this
		this.viewer = {}
		this.parentEl = $('#'+parentId)
		this.codeEdit = new mCodeEdit()
		this.highlighter = new mHighlighter.Highlighter()
		this.highlighter.addEventListener("StyleReady", function(styleReadyEvent) {
			styleReadyEvent.type = "orion.edit.highlighter.styleReady"
		//	highlighterServiceImpl.dispatchEvent(styleReadyEvent)
		})
		
		this.codeEdit.serviceRegistry.registerService(
			"orion.edit.contentAssist",
			{
				computeProposals: function(buffer, offset, context) {
				}
			},
			{
				name: languageId+' content assist',
				contentTypes: ["text/plain"]
			}
		)
		
		this.codeEdit.create({
			parent: parentId
		}).then(function(editorViewer) {
			editorViewer.setContents(initialContent, "text/plain")
			self.viewer = editorViewer
		    editorViewer.editor.addEventListener("InputChanged", function(evt) {
		        if(evt.contentsSaved) {
			    	//Save your editor contents;
				}
		        $(self.parentEl).trigger( "editor.InputChanged", evt )
		    });

		    this.highlighter.setAnnotationModel(editorViewer)
		})
	}

	Editor.prototype.getText = function() {
		try {
			return this.viewer.editor.getTextView().getText()
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}
	
	Editor.prototype.updateParseTree = function(parseTree) {
		this.highlighter.update(parseTree)
	}

	return Editor
})