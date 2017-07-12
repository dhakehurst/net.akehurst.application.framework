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
        "jquery"          : "jquery/3.1.0/jquery.min",
		"sockjs"          : "sockjs-client/1.1.1/sockjs.min",
		"chartjs"         : "chartjs/2.4.0/Chart.min",
		"lodash"          : "lodash/3.10.1/lodash.min",
		"graphlib"        : "graphlib/2.1.1/dist/graphlib.min",
		"dagre"           : "dagre/0.7.4/dist/dagre.core.min",
		"cytoscape"       : "cytoscape/3.1.1/dist/cytoscape.min",
		"cytoscape-dagre" : "cytoscape-dagre/1.6.0/cytoscape-dagre",
		"backbone"        : "backbone/1.3.3/backbone-min",
		"jointjs"         : "jointjs/1.0.3/dist/joint.min",
		'jointjs-loader-hack': 'dynamic/1.0.alpha.20/jointjs-loader-hack',
		"crypto.pbkdf2"   : "cryptojs/3.1.2/rollups/pbkdf2",
		"crypto.aes"      : "cryptojs/3.1.2/rollups/aes",
		"Table"         : "dynamic/1.0.alpha.20/Table",
		"Dialog"         : "dynamic/1.0.alpha.20/Dialog",
		"Diagram"         : "dynamic/1.0.alpha.20/Diagram",
		"Graph"         : "dynamic/1.0.alpha.20/Graph",
		"Editor"         : "dynamic/1.0.alpha.20/EditorMonaco",
		"Dynamic"         : "dynamic/1.0.alpha.20/Dynamic",
		"ServerComms"     : "dynamic/1.0.alpha.20/ServerComms",
		"Highlighter"     : "dynamic/1.0.alpha.20/Highlighter",
		"orion/code_edit" : "orion/12.0/code_edit/built-codeEdit-amd",
//		"vscode/languageclient" : "languageclient/2.5.0/main",
		"styles/normalize" : "normalize.css/5.0.0/normalize",
		"vs"           : "monaco-editor/0.9.0/min/vs"
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
            'underscore': 'lodash'
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