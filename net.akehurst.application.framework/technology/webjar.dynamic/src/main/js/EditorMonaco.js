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

	function Editor(parentId, languageId, initialContent, options, comms) {
		let self = this
		this.languageId = languageId
		this.viewer = {}
		this.parentEl = $('#'+parentId)
		this.decorations = []
		this.comms = comms
		monaco.languages.register({ id: languageId })
		//monaco.languages.onLanguage(function(languageId) {
			self.initLanguage(languageId)
		//})
		
		let v = $.extend({ language: languageId, value: initialContent }, options)
			
		this.editor = monaco.editor.create(document.getElementById(parentId), v)
		this.editor.onDidChangeModelContent(function(evt) {
			$(self.parentEl).trigger( "oninput", evt )
		})

	}

	Editor.prototype = {
		initLanguage : function(languageId) {
			

//			monaco.languages.registerDocumentFormattingEditProvider(languageId,{
//				provideDocumentFormattingEdits: (model, options, cancellationToken) => {
//					
//				}
//			})
			
			//this.editor.setModelLanguage(??)
			// Register a completion item provider for the new language
			monaco.languages.registerCompletionItemProvider(languageId, {
				provideCompletionItems: (model, position, cancellationToken) => {
					let promise = this.comms.call("Editor.provideCompletionItems", {text:model.getValue(), position:model.getOffsetAt(position)})
					return promise.then((data)=>{ return data.result })
//					return [
//						{
//							label: 'simpleText',
//							kind: monaco.languages.CompletionItemKind.Text
//						}
//					]
				}
			})
			
//			monaco.languages.registerDefinitionProvider(languageId, {
//				provideDefinition : function(model, position, cancellationToken) {
//					
//				}
//			})

		},
		
		getText : function() {
			try {
				return this.editor.getModel().getValue()
			} catch (err) {
				console.log("Error: "+err.message)
			}
		},

		setText : function(text) {
			try {
				return this.editor.getModel().setValue(text)
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
				if (null != node.name) {
					let start = this.editor.getModel().getPositionAt(node.start)
					let end = this.editor.getModel().getPositionAt(node.start+node.length)
					let dec = this.createDecoration(start, end, node.name, node.name.replace(/_/g,'-'))
					decors.push(dec)
				}
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