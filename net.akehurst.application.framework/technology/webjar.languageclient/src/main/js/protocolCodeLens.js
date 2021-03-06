/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
'use strict';
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var code = require('vscode');
var ProtocolCodeLens = (function (_super) {
    __extends(ProtocolCodeLens, _super);
    function ProtocolCodeLens(range) {
        _super.call(this, range);
    }
    return ProtocolCodeLens;
}(code.CodeLens));
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = ProtocolCodeLens;
