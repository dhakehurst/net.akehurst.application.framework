import * as api from './Editor'

import * as monaco from 'monaco-editor'
//import {Observable, Subject} from 'rxjs';

export class EditorMonaco implements api.Editor {
  
  private monacoEditor: monaco.editor.IStandaloneCodeEditor;
  
  constructor(parentId: string) {
    let languageId = 'xxx';
    let initialContent = '';
    let options = {};
    //let v = $.extend({ language: languageId, value: initialContent }, options);
    let v = { language: languageId, value: initialContent };
  	this.monacoEditor = monaco.editor.create(document.getElementById(parentId), v);
  }
  
  get monaco() {
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
  }
      
  updateParseTree(tree: api.SharedPackedParseTree) : void {
  }
  
  
}
