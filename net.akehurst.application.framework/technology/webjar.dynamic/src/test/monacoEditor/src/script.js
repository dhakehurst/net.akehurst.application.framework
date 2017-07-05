"use strict"

require.config({
	baseUrl: "./",
    paths: {
        "jquery"                : "../node_modules/jquery/dist/jquery.min",
        "Editor"                : "EditorMonaco",
		'vs'                    : "../node_modules/monaco-editor/dev/vs",
    },
    shim: {
    },
    map: {
    },
    bundles: {
    }	
});

require(['jquery'],function($){

	$(document).ready(function() {
	
        $('#openDialog').on('click', function(event) {
        	let container = document.createElement('div')
        	document.body.appendChild(container)
        	$(container).addClass('dialog-backdrop')
      
            let dialog = document.createElement('dialog')
            dialog.id = 'dialogForEditor'
            container.appendChild(dialog)
            
            dialog.insertAdjacentHTML('beforeend', "<div id='textEditor1' class='editor'></div>")
            dialog.insertAdjacentHTML('beforeend', "<div id='textEditor2' class='editor'></div>")
            dialog.insertAdjacentHTML('beforeend', "<button id='closeDialog'>Close</button>")
            $(dialog).attr('open',true)
            //dialog.showModal()
            $('#closeDialog').on('click', function(event) {
                $(container).remove()
            })
            
            // code to create and initialise the editor
            require(['Editor'],function(Editor){
                let e1 = new Editor('textEditor1')
                e1.init()
                
                let e2 = new Editor('textEditor2')
                e2.init()
            })
            
            
        })
		
	})

})