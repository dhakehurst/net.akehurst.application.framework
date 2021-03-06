/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
'use strict';
var code = require('vscode');
var ls = require('vscode-languageserver-types');
var is = require('./utils/is');
var protocolCompletionItem_1 = require('./protocolCompletionItem');
var protocolCodeLens_1 = require('./protocolCodeLens');
function createConverter(uriConverter) {
    var nullConverter = function (value) { return value.toString(); };
    var _uriConverter = uriConverter || nullConverter;
    function asUri(value) {
        return _uriConverter(value);
    }
    function asTextDocumentIdentifier(textDocument) {
        return {
            uri: _uriConverter(textDocument.uri)
        };
    }
    function asOpenTextDocumentParams(textDocument) {
        return {
            textDocument: {
                uri: _uriConverter(textDocument.uri),
                languageId: textDocument.languageId,
                version: textDocument.version,
                text: textDocument.getText()
            }
        };
    }
    function isTextDocumentChangeEvent(value) {
        var candidate = value;
        return is.defined(candidate.document) && is.defined(candidate.contentChanges);
    }
    function isTextDocument(value) {
        var candidate = value;
        return is.defined(candidate.uri) && is.defined(candidate.version);
    }
    function asChangeTextDocumentParams(arg) {
        if (isTextDocument(arg)) {
            var result = {
                textDocument: {
                    uri: _uriConverter(arg.uri),
                    version: arg.version
                },
                contentChanges: [{ text: arg.getText() }]
            };
            return result;
        }
        else if (isTextDocumentChangeEvent(arg)) {
            var document_1 = arg.document;
            var result = {
                textDocument: {
                    uri: _uriConverter(document_1.uri),
                    version: document_1.version
                },
                contentChanges: arg.contentChanges.map(function (change) {
                    var range = change.range;
                    return {
                        range: {
                            start: { line: range.start.line, character: range.start.character },
                            end: { line: range.end.line, character: range.end.character }
                        },
                        rangeLength: change.rangeLength,
                        text: change.text
                    };
                })
            };
            return result;
        }
        else {
            throw Error('Unsupported text document change parameter');
        }
    }
    function asCloseTextDocumentParams(textDocument) {
        return {
            textDocument: asTextDocumentIdentifier(textDocument)
        };
    }
    function asSaveTextDocumentParams(textDocument) {
        return {
            textDocument: asTextDocumentIdentifier(textDocument)
        };
    }
    function asTextDocumentPositionParams(textDocument, position) {
        return {
            textDocument: asTextDocumentIdentifier(textDocument),
            position: asWorkerPosition(position)
        };
    }
    function asWorkerPosition(position) {
        return { line: position.line, character: position.character };
    }
    function asRange(value) {
        if (is.undefined(value)) {
            return undefined;
        }
        else if (is.nil(value)) {
            return null;
        }
        return { start: asPosition(value.start), end: asPosition(value.end) };
    }
    function asPosition(value) {
        if (is.undefined(value)) {
            return undefined;
        }
        else if (is.nil(value)) {
            return null;
        }
        return { line: value.line, character: value.character };
    }
    function set(value, func) {
        if (is.defined(value)) {
            func();
        }
    }
    function asDiagnosticSeverity(value) {
        switch (value) {
            case code.DiagnosticSeverity.Error:
                return 1 /* Error */;
            case code.DiagnosticSeverity.Warning:
                return 2 /* Warning */;
            case code.DiagnosticSeverity.Information:
                return 3 /* Information */;
            case code.DiagnosticSeverity.Hint:
                return 4 /* Hint */;
        }
    }
    function asDiagnostic(item) {
        var result = ls.Diagnostic.create(asRange(item.range), item.message);
        set(item.severity, function () { return result.severity = asDiagnosticSeverity(item.severity); });
        set(item.code, function () { return result.code = item.code; });
        set(item.source, function () { return result.source = item.source; });
        return result;
    }
    function asDiagnostics(items) {
        if (is.undefined(items) || is.nil(items)) {
            return items;
        }
        return items.map(asDiagnostic);
    }
    function asCompletionItem(item) {
        var result = { label: item.label };
        set(item.detail, function () { return result.detail = item.detail; });
        set(item.documentation, function () { return result.documentation = item.documentation; });
        set(item.filterText, function () { return result.filterText = item.filterText; });
        set(item.insertText, function () { return result.insertText = item.insertText; });
        // Protocol item kind is 1 based, codes item kind is zero based.
        set(item.kind, function () { return result.kind = item.kind + 1; });
        set(item.sortText, function () { return result.sortText = item.sortText; });
        set(item.textEdit, function () { return result.textEdit = asTextEdit(item.textEdit); });
        if (item instanceof protocolCompletionItem_1.default) {
            set(item.data, function () { return result.data = item.data; });
        }
        return result;
    }
    function asTextEdit(edit) {
        return { range: asRange(edit.range), newText: edit.newText };
    }
    function asReferenceParams(textDocument, position, options) {
        return {
            textDocument: asTextDocumentIdentifier(textDocument),
            position: asWorkerPosition(position),
            context: { includeDeclaration: options.includeDeclaration }
        };
    }
    function asCodeActionContext(context) {
        if (is.undefined(context) || is.nil(context)) {
            return context;
        }
        return ls.CodeActionContext.create(asDiagnostics(context.diagnostics));
    }
    function asCommand(item) {
        var result = ls.Command.create(item.title, item.command);
        if (is.defined(item.arguments))
            result.arguments = item.arguments;
        return result;
    }
    function asCodeLens(item) {
        var result = ls.CodeLens.create(asRange(item.range));
        if (is.defined(item.command))
            result.command = asCommand(item.command);
        if (item instanceof protocolCodeLens_1.default) {
            if (is.defined(item.data))
                result.data = item.data;
        }
        return result;
    }
    function asFormattingOptions(item) {
        return { tabSize: item.tabSize, insertSpaces: item.insertSpaces };
    }
    function asDocumentSymbolParams(textDocument) {
        return {
            textDocument: asTextDocumentIdentifier(textDocument)
        };
    }
    function asCodeLensParams(textDocument) {
        return {
            textDocument: asTextDocumentIdentifier(textDocument)
        };
    }
    return {
        asUri: asUri,
        asTextDocumentIdentifier: asTextDocumentIdentifier,
        asOpenTextDocumentParams: asOpenTextDocumentParams,
        asChangeTextDocumentParams: asChangeTextDocumentParams,
        asCloseTextDocumentParams: asCloseTextDocumentParams,
        asSaveTextDocumentParams: asSaveTextDocumentParams,
        asTextDocumentPositionParams: asTextDocumentPositionParams,
        asWorkerPosition: asWorkerPosition,
        asRange: asRange,
        asPosition: asPosition,
        asDiagnosticSeverity: asDiagnosticSeverity,
        asDiagnostic: asDiagnostic,
        asDiagnostics: asDiagnostics,
        asCompletionItem: asCompletionItem,
        asTextEdit: asTextEdit,
        asReferenceParams: asReferenceParams,
        asCodeActionContext: asCodeActionContext,
        asCommand: asCommand,
        asCodeLens: asCodeLens,
        asFormattingOptions: asFormattingOptions,
        asDocumentSymbolParams: asDocumentSymbolParams,
        asCodeLensParams: asCodeLensParams
    };
}
exports.createConverter = createConverter;
// This for backward compatibility since we exported the converter functions as API.
var defaultConverter = createConverter();
exports.asTextDocumentIdentifier = defaultConverter.asTextDocumentIdentifier;
exports.asOpenTextDocumentParams = defaultConverter.asOpenTextDocumentParams;
exports.asChangeTextDocumentParams = defaultConverter.asChangeTextDocumentParams;
exports.asCloseTextDocumentParams = defaultConverter.asCloseTextDocumentParams;
exports.asSaveTextDocumentParams = defaultConverter.asSaveTextDocumentParams;
exports.asTextDocumentPositionParams = defaultConverter.asTextDocumentPositionParams;
exports.asWorkerPosition = defaultConverter.asWorkerPosition;
exports.asRange = defaultConverter.asRange;
exports.asPosition = defaultConverter.asPosition;
exports.asDiagnosticSeverity = defaultConverter.asDiagnosticSeverity;
exports.asDiagnostic = defaultConverter.asDiagnostic;
exports.asDiagnostics = defaultConverter.asDiagnostics;
exports.asCompletionItem = defaultConverter.asCompletionItem;
exports.asTextEdit = defaultConverter.asTextEdit;
exports.asReferenceParams = defaultConverter.asReferenceParams;
exports.asCodeActionContext = defaultConverter.asCodeActionContext;
exports.asCommand = defaultConverter.asCommand;
exports.asCodeLens = defaultConverter.asCodeLens;
exports.asFormattingOptions = defaultConverter.asFormattingOptions;
exports.asDocumentSymbolParams = defaultConverter.asDocumentSymbolParams;
exports.asCodeLensParams = defaultConverter.asCodeLensParams;
