import { Component, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';

import * as nal from '../language-editor-api/Editor'
import * as nalM from '../language-editor-monaco/EditorMonaco'

@Component({
  selector: 'nal-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.css']
})
export class EditorComponent implements AfterViewInit, OnDestroy {

  @ViewChild('editorDiv') editorRef: ElementRef;
  private editor: nal.Editor;
  
  
  constructor() {
  	
  }
  
  ngAfterViewInit(): void {
    this.editor = new nalM.EditorMonaco(this.editorRef.nativeElement, 'dot', '');
  }

  ngOnDestroy() {
  }

}