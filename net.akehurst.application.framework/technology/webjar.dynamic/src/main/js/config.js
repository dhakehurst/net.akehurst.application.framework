/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict"

require.config({
	baseUrl: rootPath+"/lib",
    paths: {
        "text"            : "requirejs-text/3.2.1/text",
        "jquery"          : "jquery/3.3.1/dist/jquery.min",
        "jquery.ui"       : "jquery-ui/1.12.1/jquery-ui.min",
		"sockjs"          : "sockjs-client/1.1.4/dist/sockjs.min",
		"chartjs"         : "chartjs/2.7.0/Chart.min",
		"lodash"          : "lodash/3.10.1/lodash.min",
		"graphlib"        : "graphlib/2.0.0/dist/graphlib.core.min",
		"dagre"           : "dagre/0.7.4/dist/dagre.core.min",
		"cytoscape"       : "cytoscape/3.2.5/dist/cytoscape.min",
		"cytoscape-dagre" : "cytoscape-dagre/2.1.0/cytoscape-dagre",
		"backbone"        : "backbone/1.3.3/backbone-min",
		"jointjs"         : "jointjs/1.0.3/dist/joint.min",
		"jointjs-loader-hack": "dynamic/1.0.0-SNAPSHOT/jointjs-loader-hack",
		"crypto.pbkdf2"   : "cryptojs/3.1.2/rollups/pbkdf2",
		"crypto.aes"      : "cryptojs/3.1.2/rollups/aes",
		"gridstack"       : "gridstack/0.3.0/dist/gridstack.min",
		"gridstack.jqueryui" : "gridstack/0.3.0/dist/gridstack.jQueryUI.min",
		"Table"           : "dynamic/1.0.0-SNAPSHOT/Table",
		"Dialog"          : "dynamic/1.0.0-SNAPSHOT/Dialog",
		"Diagram"         : "dynamic/1.0.0-SNAPSHOT/Diagram",
		"Graph"           : "dynamic/1.0.0-SNAPSHOT/Graph",
		"Editor"          : "dynamic/1.0.0-SNAPSHOT/EditorMonaco",
		"Grid"            : "dynamic/1.0.0-SNAPSHOT/Grid",
		"AFChart"         : "dynamic/1.0.0-SNAPSHOT/AFChart",
		"Dynamic"         : "dynamic/1.0.0-SNAPSHOT/Dynamic",
		"ServerComms"     : "dynamic/1.0.0-SNAPSHOT/ServerComms",
//		"Highlighter"     : "dynamic/1.0.0-SNAPSHOT/Highlighter",
//		"orion/code_edit" : "orion/12.0/code_edit/built-codeEdit-amd",
//		"vscode/languageclient" : "languageclient/2.5.0/main",
//		"styles/normalize" : "normalize.css/5.0.0/normalize",
		"vs"           : "monaco-editor/0.10.0/min/vs"
//override vs tokenisation support
//		"vs/editor/common/modes/supports/tokenization" : "dynamic/1.0.0-SNAPSHOT/tokenization"
    },
    shim: {
        'graphlib': ['lodash'],
        'dagre': ['graphlib'],
        'jointjs': ['dagre', 'graphlib'],
        'Editor': ['vs/loader','vs/editor/editor.main'], //['orion/code_edit'],
        'Graph': ['cytoscape', 'cytoscape-dagre'],
        'Dynamic':[]
    },
    map: {
        '*': {
            'css': 'require-css/0.1.10/css',
            'underscore': 'lodash',
            //for gridstack!
            'jquery-ui/data': 'jquery.ui',
            'jquery-ui/disable-selection': 'jquery.ui',
            'jquery-ui/focusable': 'jquery.ui',
            'jquery-ui/form': 'jquery.ui',
            'jquery-ui/ie': 'jquery.ui',
            'jquery-ui/keycode': 'jquery.ui',
            'jquery-ui/labels': 'jquery.ui',
            'jquery-ui/jquery-1-7': 'jquery.ui',
            'jquery-ui/plugin': 'jquery.ui',
            'jquery-ui/safe-active-element': 'jquery.ui',
            'jquery-ui/safe-blur': 'jquery.ui',
            'jquery-ui/scroll-parent': 'jquery.ui',
            'jquery-ui/tabbable': 'jquery.ui',
            'jquery-ui/unique-id': 'jquery.ui',
            'jquery-ui/version': 'jquery.ui',
            'jquery-ui/widget': 'jquery.ui',
            'jquery-ui/widgets/mouse': 'jquery.ui',
            'jquery-ui/widgets/draggable': 'jquery.ui',
            'jquery-ui/widgets/droppable': 'jquery.ui',
            'jquery-ui/widgets/resizable': 'jquery.ui'
        },
        'jointjs': {
            'backbone': 'jointjs-loader-hack'
        }
    },
    bundles: {
        "editorBuild/code_edit/built-codeEdit-amd": ["orion/codeEdit", "orion/Deferred"]
    }	
});

//require(['jquery','Dynamic','css!styles/normalize'],function($, Dynamic){
require(['jquery','Dynamic'],function($, Dynamic){

	$(document).ready(function() {
		new Dynamic(rootPath, stageId)
	})

})