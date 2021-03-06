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
    var nullConverter = function (value) { return code.Uri.parse(value); };
    var _uriConverter = uriConverter || nullConverter;
    function asUri(value) {
        return _uriConverter(value);
    }
    function asDiagnostics(diagnostics) {
        return diagnostics.map(asDiagnostic);
    }
    function asDiagnostic(diagnostic) {
        var result = new code.Diagnostic(asRange(diagnostic.range), diagnostic.message, asDiagnosticSeverity(diagnostic.severity));
        if (is.defined(diagnostic.code)) {
            result.code = diagnostic.code;
        }
        if (is.defined(diagnostic.source)) {
            result.source = diagnostic.source;
        }
        return result;
    }
    function asRange(value) {
        if (is.undefined(value)) {
            return undefined;
        }
        else if (is.nil(value)) {
            return null;
        }
        return new code.Range(asPosition(value.start), asPosition(value.end));
    }
    function asPosition(value) {
        if (is.undefined(value)) {
            return undefined;
        }
        else if (is.nil(value)) {
            return null;
        }
        return new code.Position(value.line, value.character);
    }
    function asDiagnosticSeverity(value) {
        if (is.undefined(value) || is.nil(value)) {
            return code.DiagnosticSeverity.Error;
        }
        switch (value) {
            case 1 /* Error */:
                return code.DiagnosticSeverity.Error;
            case 2 /* Warning */:
                return code.DiagnosticSeverity.Warning;
            case 3 /* Information */:
                return code.DiagnosticSeverity.Information;
            case 4 /* Hint */:
                return code.DiagnosticSeverity.Hint;
        }
        return code.DiagnosticSeverity.Error;
    }
    function asHover(hover) {
        if (is.undefined(hover)) {
            return undefined;
        }
        if (is.nil(hover)) {
            return null;
        }
        return new code.Hover(hover.contents, is.defined(hover.range) ? asRange(hover.range) : undefined);
    }
    function asCompletionResult(result) {
        if (Array.isArray(result)) {
            var items = result;
            return items.map(asCompletionItem);
        }
        var list = result;
        return new code.CompletionList(list.items.map(asCompletionItem), list.isIncomplete);
    }
    function set(value, func) {
        if (is.defined(value)) {
            func();
        }
    }
    function asCompletionItem(item) {
        var result = new protocolCompletionItem_1.default(item.label);
        set(item.detail, function () { return result.detail = item.detail; });
        set(item.documentation, function () { return result.documentation = item.documentation; });
        set(item.filterText, function () { return result.filterText = item.filterText; });
        set(item.insertText, function () { return result.insertText = item.insertText; });
        // Protocol item kind is 1 based, codes item kind is zero based.
        set(item.kind, function () { return result.kind = item.kind - 1; });
        set(item.sortText, function () { return result.sortText = item.sortText; });
        set(item.textEdit, function () { return result.textEdit = asTextEdit(item.textEdit); });
        set(item.data, function () { return result.data = item.data; });
        return result;
    }
    function asTextEdit(edit) {
        return new code.TextEdit(asRange(edit.range), edit.newText);
    }
    function asTextEdits(items) {
        return items.map(asTextEdit);
    }
    function asSignatureHelp(item) {
        var result = new code.SignatureHelp();
        set(item.activeParameter, function () { return result.activeParameter = item.activeParameter; });
        set(item.activeSignature, function () { return result.activeSignature = item.activeSignature; });
        set(item.signatures, function () { return result.signatures = asSignatureInformations(item.signatures); });
        return result;
    }
    function asSignatureInformations(items) {
        return items.map(asSignatureInformation);
    }
    function asSignatureInformation(item) {
        var result = new code.SignatureInformation(item.label);
        set(item.documentation, function () { return result.documentation = item.documentation; });
        set(item.parameters, function () { return result.parameters = asParameterInformations(item.parameters); });
        return result;
    }
    function asParameterInformations(item) {
        return item.map(asParameterInformation);
    }
    function asParameterInformation(item) {
        var result = new code.ParameterInformation(item.label);
        set(item.documentation, function () { return result.documentation = item.documentation; });
        return result;
    }
    function asDefinitionResult(item) {
        if (is.array(item)) {
            return item.map(asLocation);
        }
        else {
            return asLocation(item);
        }
    }
    function asLocation(item) {
        if (is.undefined(item)) {
            return undefined;
        }
        if (is.nil(item)) {
            return null;
        }
        return new code.Location(_uriConverter(item.uri), asRange(item.range));
    }
    function asReferences(values) {
        return values.map(asLocation);
    }
    function asDocumentHighlights(values) {
        return values.map(asDocumentHighlight);
    }
    function asDocumentHighlight(item) {
        var result = new code.DocumentHighlight(asRange(item.range));
        set(item.kind, function () { return result.kind = asDocumentHighlightKind(item.kind); });
        return result;
    }
    function asDocumentHighlightKind(item) {
        switch (item) {
            case 1 /* Text */:
                return code.DocumentHighlightKind.Text;
            case 2 /* Read */:
                return code.DocumentHighlightKind.Read;
            case 3 /* Write */:
                return code.DocumentHighlightKind.Write;
        }
        return code.DocumentHighlightKind.Text;
    }
    function asSymbolInformations(values, uri) {
        return values.map(function (information) { return asSymbolInformation(information, uri); });
    }
    function asSymbolInformation(item, uri) {
        // Symbol kind is one based in the protocol and zero based in code.
        var result = new code.SymbolInformation(item.name, item.kind - 1, asRange(item.location.range), item.location.uri ? _uriConverter(item.location.uri) : uri);
        set(item.containerName, function () { return result.containerName = item.containerName; });
        return result;
    }
    function asCommand(item) {
        var result = { title: item.title, command: item.command };
        set(item.arguments, function () { return result.arguments = item.arguments; });
        return result;
    }
    function asCommands(items) {
        return items.map(asCommand);
    }
    function asCodeLens(item) {
        var result = new protocolCodeLens_1.default(asRange(item.range));
        if (is.defined(item.command))
            result.command = asCommand(item.command);
        if (is.defined(item.data))
            result.data = item.data;
        return result;
    }
    function asCodeLenses(items) {
        return items.map(asCodeLens);
    }
    function asWorkspaceEdit(item) {
        var result = new code.WorkspaceEdit();
        var keys = Object.keys(item.changes);
        keys.forEach(function (key) { return result.set(_uriConverter(key), asTextEdits(item.changes[key])); });
        return result;
    }
    return {
        asUri: asUri,
        asDiagnostics: asDiagnostics,
        asDiagnostic: asDiagnostic,
        asRange: asRange,
        asPosition: asPosition,
        asDiagnosticSeverity: asDiagnosticSeverity,
        asHover: asHover,
        asCompletionResult: asCompletionResult,
        asCompletionItem: asCompletionItem,
        asTextEdit: asTextEdit,
        asTextEdits: asTextEdits,
        asSignatureHelp: asSignatureHelp,
        asSignatureInformations: asSignatureInformations,
        asSignatureInformation: asSignatureInformation,
        asParameterInformations: asParameterInformations,
        asParameterInformation: asParameterInformation,
        asDefinitionResult: asDefinitionResult,
        asLocation: asLocation,
        asReferences: asReferences,
        asDocumentHighlights: asDocumentHighlights,
        asDocumentHighlight: asDocumentHighlight,
        asDocumentHighlightKind: asDocumentHighlightKind,
        asSymbolInformations: asSymbolInformations,
        asSymbolInformation: asSymbolInformation,
        asCommand: asCommand,
        asCommands: asCommands,
        asCodeLens: asCodeLens,
        asCodeLenses: asCodeLenses,
        asWorkspaceEdit: asWorkspaceEdit
    };
}
exports.createConverter = createConverter;
// This for backward compatibility since we exported the converter functions as API.
var defaultConverter = createConverter();
exports.asDiagnostics = defaultConverter.asDiagnostics;
exports.asDiagnostic = defaultConverter.asDiagnostic;
exports.asRange = defaultConverter.asRange;
exports.asPosition = defaultConverter.asPosition;
exports.asDiagnosticSeverity = defaultConverter.asDiagnosticSeverity;
exports.asHover = defaultConverter.asHover;
exports.asCompletionResult = defaultConverter.asCompletionResult;
exports.asCompletionItem = defaultConverter.asCompletionItem;
exports.asTextEdit = defaultConverter.asTextEdit;
exports.asTextEdits = defaultConverter.asTextEdits;
exports.asSignatureHelp = defaultConverter.asSignatureHelp;
exports.asSignatureInformations = defaultConverter.asSignatureInformations;
exports.asSignatureInformation = defaultConverter.asSignatureInformation;
exports.asParameterInformations = defaultConverter.asParameterInformations;
exports.asParameterInformation = defaultConverter.asParameterInformation;
exports.asDefinitionResult = defaultConverter.asDefinitionResult;
exports.asLocation = defaultConverter.asLocation;
exports.asReferences = defaultConverter.asReferences;
exports.asDocumentHighlights = defaultConverter.asDocumentHighlights;
exports.asDocumentHighlight = defaultConverter.asDocumentHighlight;
exports.asDocumentHighlightKind = defaultConverter.asDocumentHighlightKind;
exports.asSymbolInformations = defaultConverter.asSymbolInformations;
exports.asSymbolInformation = defaultConverter.asSymbolInformation;
exports.asCommand = defaultConverter.asCommand;
exports.asCommands = defaultConverter.asCommands;
exports.asCodeLens = defaultConverter.asCodeLens;
exports.asCodeLenses = defaultConverter.asCodeLenses;
exports.asWorkspaceEdit = defaultConverter.asWorkspaceEdit;
