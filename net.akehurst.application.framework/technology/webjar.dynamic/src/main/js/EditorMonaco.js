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
		this.editor = monaco.editor.create(document.getElementById(parentId), {
			value: initialContent
		})

	}

	Editor.prototype.getText = function() {
//		try {
//			return this.viewer.editor.getTextView().getText()
//		} catch (err) {
//			console.log("Error: "+err.message)
			return ""
//		}
	}
	
	Editor.prototype.updateParseTree = function(parseTree) {
//		this.highlighter.update(parseTree)
	}

	return Editor
})