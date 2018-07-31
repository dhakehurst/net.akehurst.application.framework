import * as api from '../language-editor-api/Editor'

// for more fetures see https://github.com/Microsoft/monaco-editor-samples/blob/master/browser-esm-webpack-small/index.js
import 'monaco-editor/esm/vs/editor/browser/controller/coreCommands';
import 'monaco-editor/esm/vs/editor/contrib/find/findController';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';

//import {Observable, Subject} from 'rxjs';
var self={ MonacoEnvironment:{} };
self.MonacoEnvironment = {
	getWorkerUrl: function (moduleId, label) {
		return './main.js';
	}
}

export class EditorMonaco implements api.Editor {
  
  private monacoEditor: monaco.editor.IStandaloneCodeEditor;
  private onChangeHandler: (text: string)=>void;
  private languageTheme: string
  
  constructor(private parentElement: any, private languageId: string, private initialContent: string) {
    this.languageTheme = languageId+'-theme';
    let options = {};
    
    monaco.languages.register({ id: languageId });
    
    //let v = $.extend({ language: languageId, value: initialContent, theme: this.languageTheme }, options);
    let v = { language: languageId, value: initialContent };
  	this.monacoEditor = monaco.editor.create(parentElement, v);
  	
  	this.monacoEditor.onDidChangeModelContent((evt:any)=> {
  	  const text = this.getText();
	  if (this.onChangeHandler) { this.onChangeHandler(text); }
	});
    
  }
  
  getMonaco() {
    return this.monacoEditor;
  }
  
  // --- api.Editor ---
  
  getText() : string {
    try {
      return this.monacoEditor.getModel().getValue()
    } catch (err) {
      console.log("Error: "+err.message)
    }
  }
      
  setText(value:string) : void {
    try {
      return this.monacoEditor.getModel().setValue(value)
    } catch (err) {
      console.log("Error: "+err.message)
    }
  }
      
  onChange(handler: (text: string)=>void) : void {
    this.onChangeHandler = handler;
  }
  
  updateSyntaxHighlightingRules() {
  
    monaco.languages.setMonarchTokensProvider(this.languageId, {
      tokenPostfix: '',
	  tokenizer: {
		root: [
		]
	  }
    });
    
    monaco.editor.defineTheme(this.languageTheme, {
	  base: 'vs',
	  inherit: false,
	  rules: [
	  ],
	  colors: {}
    });    
    
  }
  
  
  updateParseTree(tree: api.SharedPackedParseTree) : void {
  }
  
  
}
