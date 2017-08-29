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
	 'vs/loader','vs/editor/editor.main','vs/editor/standalone/browser/standaloneServices'
], function(loader,main,standaloneServices) {

	function LineState(lineNum, lineOffsetFromStart) {
		this.lineNum = lineNum
		this.lineOffsetFromStart = lineOffsetFromStart
	}
	LineState.prototype = {
		clone:function() { return new LineState(this.lineNum, this.lineOffsetFromStart) },
		equals:function(other) { return this.lineNum == other.lineNum }
	}
	
	
	function TokensProvider() {
		this.model = {}
		this.lineTokens = []
	}
	TokensProvider.prototype = {
		getInitialState : function() {
			return new LineState(0,0)
		},
		tokenize : function(lineText, state) {
			let lineOffsetFromStart = state.lineOffsetFromStart + lineText.length
			let lineNum = state.lineNum
			let tokens = this.lineTokens[lineNum]
			if (null==tokens) {
				tokens = [] //{ startIndex:0, scopes:'GRAPH.dot' }, { startIndex:5, scopes:'WS.dot' }]
			}
			let adjustedTokens = []
			for (let i = 0, len = tokens.length; i < len; i++) {
				const t = tokens[i]
				let adj = state.lineOffsetFromStart + lineNum // +lineNum for the eol chars
				let aTok = { startIndex:t.startIndex - adj, scopes:t.scopes }
				adjustedTokens.push(aTok)
			}
			let lineTokens = {
				tokens: adjustedTokens,
				endState: new LineState(lineNum+1,lineOffsetFromStart)
			}
			return lineTokens
		},
		updateLineTokens(lineNum, lineTokens) {
			//let binTokens = this._toBinaryTokens(lineTokens)
			//this.editor.getModel()._lines[lineNum].setTokens(this.languageId, binTokens)
			this.lineTokens[lineNum] = lineTokens
		},
		appendScope(node, scopes) {
			let newScopes = scopes
			if (node.isPattern) {
				//can't use pattern name as a scope name
			} else {
				if (null != node.name) {
					if (''==newScopes) {
						newScopes = node.name
					} else {
						newScopes = scopes+'.'+node.name
					}
				}
			}
			return newScopes
		},
		updateLineTokensLeaf(curInfo, leaf, scopes) {
			if (0==leaf.length) {
				//do nothing
				return curInfo
			} else {
				let newScopes = scopes //this.appendScope(leaf,scopes)
				
				//here startIndex the offset from start of text
				let token = { startIndex:leaf.start, scopes:newScopes }
				let newTokens = curInfo.curTokens
				newTokens.push(token)
				if (null==leaf.numEol || 0==leaf.numEol) {
					return {curTokens:newTokens,curLineNum:curInfo.curLineNum} 
				} else {
					//TODO: handle leaf that crosses more than one line
					this.updateLineTokens(curInfo.curLineNum, newTokens)
					return {curTokens:[],curLineNum:curInfo.curLineNum+1} 
				}
			}
		},
		updateLineTokensNode(curInfo, node, scopes) {
			let newScopes = this.appendScope(node,scopes)
			let newInfo = curInfo
			if (null != node.children) {
				for(let i=0, len=node.children.length; i < len; i++) {
					let child = node.children[i]
					newInfo = this.updateLineTokensNode(newInfo, child, newScopes)
				}
			} else {
				newInfo = this.updateLineTokensLeaf(newInfo, node, newScopes)
			}
			return newInfo
		}
	}
	
	function Editor(parentId, languageId, initialContent, options, comms) {
		let self = this
		this.languageId = languageId
		this.viewer = {}
		this.parentEl = $('#'+parentId)
		this.decorations = []
		this.comms = comms
		
		this.tokensProvider = new TokensProvider()
		monaco.languages.register({ id: languageId })
		//monaco.languages.onLanguage(function(languageId) {
			self.initLanguage(languageId)
		//})
		
		let v = $.extend({ language: languageId, value: initialContent }, options)
			
		this.editor = monaco.editor.create(document.getElementById(parentId), v)
		this.editor.onDidChangeModelContent(function(evt) {
			$(self.parentEl).trigger( "change", evt )
		})
		this.tokensProvider.model = this.editor.getModel()
		
//		this.editor.getModel()._updateTokensUntilLine = function(eventBuilder, lineNumber) {
//			//do nothing, handled by updateParseTree
//		}

	}

	Editor.match = function(useWildcards,rule, token) {
		if (token === '') {
			return rule._mainRule
		}

		let dotIndex = token.indexOf('.')
		let head = ''
		let tail = ''
		if (dotIndex === -1) {
			head = token
			tail = ''
		} else {
			head = token.substring(0, dotIndex)
			tail = token.substring(dotIndex + 1)
		}

		let match = undefined
		
		//first check for an any path matcher
		let path = rule._children.get('**')
		if (typeof path !== 'undefined') {
			let child = path._children.get(head)
			while (tail !== '' && typeof match==='undefined') {
				dotIndex = tail.indexOf('.')
				if (dotIndex === -1) {
					head = tail
					tail = ''
				} else {
					head = tail.substring(0, dotIndex)
					tail = tail.substring(dotIndex + 1)
				}	
				child = path._children.get(head)
				if (typeof child !== 'undefined') {
					match = Editor.match(useWildcards,child,tail);
				}
			}
			if (typeof match !== 'undefined') {
				//use match
			} else {
				// tail must === '', return path rule
				match = Editor.match(useWildcards,path,tail);
			}
		} else {
			match = undefined
		}
		
		// try match exact head
		if (typeof match === 'undefined') {
			let child = rule._children.get(head)
			if (typeof child !== 'undefined') {
				match = Editor.match(useWildcards,child,tail);
			}
		}
		//try wild card single element match
		if (typeof match === 'undefined') {
			let star = rule._children.get('*')
			if (typeof star !== 'undefined') {
				match = Editor.match(useWildcards,star,tail);
			} else {
				match = undefined
			}
		}
		
		// if match is still undefined, return this rule 
		//if (typeof match === 'undefined') {
		//	match = rule._mainRule
		//}
		return match
	}
	
	Editor.defineTheme = function(name, rules) {
		let theme = { base:'vs', inherit:true, rules:rules }
		monaco.editor.defineTheme(name,theme)
		
		let themeService = standaloneServices.StaticServices.standaloneThemeService.get()
		let tokenTheme = themeService.getTheme().tokenTheme
		
		tokenTheme.__proto__._match = function(token) {
			return Editor.match(this._root, this._root, token)
		}
		
	}
	
	Editor.prototype = {
		initLanguage : function(languageId) {
			monaco.languages.setTokensProvider(this.languageId, this.tokensProvider)

//			monaco.languages.registerDocumentFormattingEditProvider(languageId,{
//				provideDocumentFormattingEdits: (model, options, cancellationToken) => {
//					
//				}
//			})
			
			
//			monaco.languages.registerDocumentHighlightProvider(languageId, {
//				provideDocumentHighlights : (model, position, cancellationToken) => {
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
//			let decors = this.doNode(parseTree, [])
//			this.decorations = this.editor.getModel().deltaDecorations(this.decorations, decors)
			//TODO: get tokenizer to work with colouring,
			// currently problem with multiple lines (leaf doesn't know about lines, only offset from start
			// theme doesn't work how we want it to
			if (null!=parseTree) {
				let tokenInfo = this.tokensProvider.updateLineTokensNode({curTokens:[],curLineNum:0}, parseTree, this.languageId)
				this.tokensProvider.updateLineTokens(tokenInfo.curLineNum, tokenInfo.curTokens)
				this.editor.getModel()._resetTokenizationState()
			}
		},
		_toBinaryTokens: function(tokens, offsetDelta) {
			let themeService = standaloneServices.StaticServices.standaloneThemeService.get()
			let tokenTheme = themeService.getTheme().tokenTheme
			
			let result = []
			let resultLen = 0
			let previousStartIndex = 0;
			for (let i = 0, len = tokens.length; i < len; i++) {
				let t = tokens[i]
				let metadata = tokenTheme.match(this.languageId, t.scopes);
				if (resultLen > 0 && result[resultLen - 1] === metadata) {
					// same metadata
					continue;
				}

				let startIndex = t.startIndex;

				// Prevent issues stemming from a buggy external tokenizer.
//				if (i === 0) {
//					// Force first token to start at first index!
//					startIndex = 0;
//				} else if (startIndex < previousStartIndex) {
//					// Force tokens to be after one another!
//					startIndex = previousStartIndex
//				}

				result[resultLen++] = startIndex // + offsetDelta
				result[resultLen++] = metadata

				previousStartIndex = startIndex
			}

			let actualResult = new Uint32Array(resultLen)
			for (let i = 0; i < resultLen; i++) {
				actualResult[i] = result[i]
			}
			return actualResult
		}
		
	}
	
	return Editor
})