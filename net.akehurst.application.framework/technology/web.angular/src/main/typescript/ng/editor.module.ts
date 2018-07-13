import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { NalEditorComponent } from './editor.component';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [
    NalEditorComponent
  ],
  exports: [
    NalEditorComponent
  ]
})
export class NalEditorModule {

}