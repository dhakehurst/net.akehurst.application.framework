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
	baseUrl: "/lib",
    paths: {
        "text"            : "requirejs-text/3.2.1/text",
        "jquery"          : "jquery/3.1.0/jquery.min",
		"sockjs"          : "sockjs-client/1.1.1/sockjs.min",
		"chartjs"         : "chartjs/2.1.3/Chart.min",
		"cytoscape"       : "cytoscape/2.7.9/dist/cytoscape.min",
		"crypto.pbkdf2"   : "cryptojs/3.1.2/rollups/pbkdf2",
		"crypto.aes"      : "cryptojs/3.1.2/rollups/aes",
		"Dynamic"         : "dynamic/1.0.alpha.18/Dynamic",
		"ServerComms"     : "dynamic/1.0.alpha.18/ServerComms",
		"Highlighter"     : "dynamic/1.0.alpha.18/Highlighter",
		"orion/code_edit" : "orion/12.0/code_edit/built-codeEdit-amd",
		"vscode/languageclient" : "languageclient/2.5.0/main"
    }
});

require(["jquery","Dynamic"],function($, Dynamic){

	$(document).ready(function() {
		 new Dynamic(rootPath, stageId)
	})

})