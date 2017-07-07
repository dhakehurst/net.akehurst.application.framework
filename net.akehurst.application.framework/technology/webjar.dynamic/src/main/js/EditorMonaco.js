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
 
//window.MonacoEnvironment = {
//		getWorkerUrl: function(workerId, label) {
//			return 'monaco-editor-worker-loader-proxy.js';
//		}
//	};

define([
	 'vs/loader','vs/editor/editor.main'
], function(

) {

	function Editor(parentId, languageId, initialContent) {
		let self = this
		this.viewer = {}
		this.parentEl = $('#'+parentId)
		this.decorations = []
		this.editor = monaco.editor.create(document.getElementById(parentId), {
			value: initialContent
		})
		this.editor.onDidChangeModelContent(function(evt) {
			$(self.parentEl).trigger( "oninput", evt )
		})
	}

	Editor.prototype = {
		getText : function() {
			try {
				return this.editor.getValue()
			} catch (err) {
				console.log("Error: "+err.message)
			}
		},
	

		createDecoration : function(start, end, title, cssClass) {
			let range = {startLineNumber:start.lineNumber, startColumn:start.column, endLineNumber:end.lineNumber,endColumn:end.column}
			let dec = { range:range, options: { inlineClassName: cssClass, hoverMessage:title } }
			return dec
		},
		doNode : function(node, decors) {
			if (node.isPattern) {
				//can't use pattern name as a css-class
			} else {
				let start = this.editor.getModel().getPositionAt(node.start)
				let end = this.editor.getModel().getPositionAt(node.start+node.length)
				let dec = this.createDecoration(start, end, node.name, node.name.replace(/_/g,'-'))
				decors.push(dec)
			}
			if (null != node.children) {
				for(let i=0, len=node.children.length; i < len; i++) {
					let child = node.children[i]
					decors = this.doNode(child, decors)
				}
			}
			return decors
		},
		updateParseTree : function(parseTree) {
			//this.annotationModel.removeAnnotations('orion.annotation.error') //'parseTree')
			let decors = this.doNode(parseTree, [])
			this.decorations = this.editor.getModel().deltaDecorations(this.decorations, decors)
		}
	}
	
	return Editor
})