'use strict'
define([
    'jquery',
    'vs/loader',
    'vs/editor/editor.main'
], function(
    $, loader, monacoeditor
) {

    function Editor(parentId, languageDescriptor) {
        this.parentElement = $('#'+parentId)
        this.editor = {}
    }

    Editor.prototype.init = function(initialContent) {
    
        this.editor = monaco.editor.create(this.parentElement[0], {
			value: initialContent
		})
        
    }


    return Editor

})