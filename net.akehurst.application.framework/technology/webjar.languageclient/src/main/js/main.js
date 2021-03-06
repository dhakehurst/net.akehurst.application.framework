/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
'use strict';
var cp = require('child_process');
var vscode_1 = require('vscode');
var vscode_jsonrpc_1 = require('vscode-jsonrpc');
exports.ErrorCodes = vscode_jsonrpc_1.ErrorCodes;
exports.ResponseError = vscode_jsonrpc_1.ResponseError;
var vscode_languageserver_types_1 = require('vscode-languageserver-types');
exports.Range = vscode_languageserver_types_1.Range;
exports.Position = vscode_languageserver_types_1.Position;
exports.Location = vscode_languageserver_types_1.Location;
exports.TextEdit = vscode_languageserver_types_1.TextEdit;
exports.WorkspaceChange = vscode_languageserver_types_1.WorkspaceChange;
exports.TextDocumentIdentifier = vscode_languageserver_types_1.TextDocumentIdentifier;
var protocol_1 = require('./protocol');
var c2p = require('./codeConverter');
exports.Code2Protocol = c2p;
var p2c = require('./protocolConverter');
exports.Protocol2Code = p2c;
var is = require('./utils/is');
var electron = require('./utils/electron');
var processes_1 = require('./utils/processes');
var async_1 = require('./utils/async');
var ConsoleLogger = (function () {
    function ConsoleLogger() {
    }
    ConsoleLogger.prototype.error = function (message) {
        console.error(message);
    };
    ConsoleLogger.prototype.warn = function (message) {
        console.warn(message);
    };
    ConsoleLogger.prototype.info = function (message) {
        console.info(message);
    };
    ConsoleLogger.prototype.log = function (message) {
        console.log(message);
    };
    return ConsoleLogger;
}());
function createConnection(input, output, errorHandler, closeHandler) {
    var logger = new ConsoleLogger();
    var connection = vscode_jsonrpc_1.createClientMessageConnection(input, output, logger);
    connection.onError(function (data) { errorHandler(data[0], data[1], data[2]); });
    connection.onClose(closeHandler);
    var result = {
        listen: function () { return connection.listen(); },
        sendRequest: function (type, params, token) { return connection.sendRequest(type, params, token); },
        sendNotification: function (type, params) { return connection.sendNotification(type, params); },
        onNotification: function (type, handler) { return connection.onNotification(type, handler); },
        onRequest: function (type, handler) { return connection.onRequest(type, handler); },
        trace: function (value, tracer, sendNotification) {
            if (sendNotification === void 0) { sendNotification = false; }
            return connection.trace(value, tracer, sendNotification);
        },
        initialize: function (params) { return connection.sendRequest(protocol_1.InitializeRequest.type, params); },
        shutdown: function () { return connection.sendRequest(protocol_1.ShutdownRequest.type, undefined); },
        exit: function () { return connection.sendNotification(protocol_1.ExitNotification.type); },
        onLogMessage: function (handler) { return connection.onNotification(protocol_1.LogMessageNotification.type, handler); },
        onShowMessage: function (handler) { return connection.onNotification(protocol_1.ShowMessageNotification.type, handler); },
        onTelemetry: function (handler) { return connection.onNotification(protocol_1.TelemetryEventNotification.type, handler); },
        didChangeConfiguration: function (params) { return connection.sendNotification(protocol_1.DidChangeConfigurationNotification.type, params); },
        didChangeWatchedFiles: function (params) { return connection.sendNotification(protocol_1.DidChangeWatchedFilesNotification.type, params); },
        didOpenTextDocument: function (params) { return connection.sendNotification(protocol_1.DidOpenTextDocumentNotification.type, params); },
        didChangeTextDocument: function (params) { return connection.sendNotification(protocol_1.DidChangeTextDocumentNotification.type, params); },
        didCloseTextDocument: function (params) { return connection.sendNotification(protocol_1.DidCloseTextDocumentNotification.type, params); },
        didSaveTextDocument: function (params) { return connection.sendNotification(protocol_1.DidSaveTextDocumentNotification.type, params); },
        onDiagnostics: function (handler) { return connection.onNotification(protocol_1.PublishDiagnosticsNotification.type, handler); },
        dispose: function () { return connection.dispose(); }
    };
    return result;
}
(function (TransportKind) {
    TransportKind[TransportKind["stdio"] = 0] = "stdio";
    TransportKind[TransportKind["ipc"] = 1] = "ipc";
})(exports.TransportKind || (exports.TransportKind = {}));
var TransportKind = exports.TransportKind;
/**
 * An action to be performed when the connection is producing errors.
 */
(function (ErrorAction) {
    /**
     * Continue running the server.
     */
    ErrorAction[ErrorAction["Continue"] = 1] = "Continue";
    /**
     * Shutdown the server.
     */
    ErrorAction[ErrorAction["Shutdown"] = 2] = "Shutdown";
})(exports.ErrorAction || (exports.ErrorAction = {}));
var ErrorAction = exports.ErrorAction;
/**
 * An action to be performed when the connection to a server got closed.
 */
(function (CloseAction) {
    /**
     * Don't restart the server. The connection stays closed.
     */
    CloseAction[CloseAction["DoNotRestart"] = 1] = "DoNotRestart";
    /**
     * Restart the server.
     */
    CloseAction[CloseAction["Restart"] = 2] = "Restart";
})(exports.CloseAction || (exports.CloseAction = {}));
var CloseAction = exports.CloseAction;
var DefaultErrorHandler = (function () {
    function DefaultErrorHandler(name) {
        this.name = name;
        this.restarts = [];
    }
    DefaultErrorHandler.prototype.error = function (error, message, count) {
        if (count && count <= 3) {
            return ErrorAction.Continue;
        }
        return ErrorAction.Shutdown;
    };
    DefaultErrorHandler.prototype.closed = function () {
        this.restarts.push(Date.now());
        if (this.restarts.length < 5) {
            return CloseAction.Restart;
        }
        else {
            var diff = this.restarts[this.restarts.length - 1] - this.restarts[0];
            if (diff <= 3 * 60 * 1000) {
                vscode_1.window.showErrorMessage("The " + this.name + " server crashed 5 times in the last 3 minutes. The server will not be restarted.");
                return CloseAction.DoNotRestart;
            }
            else {
                this.restarts.shift();
                return CloseAction.Restart;
            }
        }
    };
    return DefaultErrorHandler;
}());
(function (State) {
    State[State["Stopped"] = 1] = "Stopped";
    State[State["Running"] = 2] = "Running";
})(exports.State || (exports.State = {}));
var State = exports.State;
var ClientState;
(function (ClientState) {
    ClientState[ClientState["Initial"] = 0] = "Initial";
    ClientState[ClientState["Starting"] = 1] = "Starting";
    ClientState[ClientState["StartFailed"] = 2] = "StartFailed";
    ClientState[ClientState["Running"] = 3] = "Running";
    ClientState[ClientState["Stopping"] = 4] = "Stopping";
    ClientState[ClientState["Stopped"] = 5] = "Stopped";
})(ClientState || (ClientState = {}));
var FalseSyncExpression = (function () {
    function FalseSyncExpression() {
    }
    FalseSyncExpression.prototype.evaluate = function (textDocument) {
        return false;
    };
    return FalseSyncExpression;
}());
var LanguageIdExpression = (function () {
    function LanguageIdExpression(_id) {
        this._id = _id;
    }
    LanguageIdExpression.prototype.evaluate = function (textDocument) {
        return this._id === textDocument.languageId;
    };
    return LanguageIdExpression;
}());
var FunctionSyncExpression = (function () {
    function FunctionSyncExpression(_func) {
        this._func = _func;
    }
    FunctionSyncExpression.prototype.evaluate = function (textDocument) {
        return this._func(textDocument);
    };
    return FunctionSyncExpression;
}());
var CompositeSyncExpression = (function () {
    function CompositeSyncExpression(values, func) {
        this._expression = values.map(function (value) { return new LanguageIdExpression(value); });
        if (func) {
            this._expression.push(new FunctionSyncExpression(func));
        }
    }
    CompositeSyncExpression.prototype.evaluate = function (textDocument) {
        return this._expression.some(function (exp) { return exp.evaluate(textDocument); });
    };
    return CompositeSyncExpression;
}());
var LanguageClient = (function () {
    function LanguageClient(arg1, arg2, arg3, arg4, arg5) {
        var _this = this;
        var clientOptions;
        var forceDebug;
        if (is.string(arg2)) {
            this._id = arg1;
            this._name = arg2;
            this._serverOptions = arg3;
            clientOptions = arg4;
            forceDebug = arg5;
        }
        else {
            this._id = arg1.toLowerCase();
            this._name = arg1;
            this._serverOptions = arg2;
            clientOptions = arg3;
            forceDebug = arg4;
        }
        if (forceDebug === void 0) {
            forceDebug = false;
        }
        this._clientOptions = clientOptions || {};
        this._clientOptions.synchronize = this._clientOptions.synchronize || {};
        this._clientOptions.errorHandler = this._clientOptions.errorHandler || new DefaultErrorHandler(this._name);
        this._syncExpression = this.computeSyncExpression();
        this._forceDebug = forceDebug;
        this.state = ClientState.Initial;
        this._connection = null;
        this._childProcess = null;
        this._outputChannel = null;
        this._listeners = null;
        this._providers = null;
        this._diagnostics = null;
        this._fileEvents = [];
        this._fileEventDelayer = new async_1.Delayer(250);
        this._onReady = new Promise(function (resolve, reject) {
            _this._onReadyCallbacks = { resolve: resolve, reject: reject };
        });
        this._telemetryEmitter = new vscode_jsonrpc_1.Emitter();
        this._stateChangeEmitter = new vscode_jsonrpc_1.Emitter();
        this._tracer = {
            log: function (message, data) {
                _this.logTrace(message, data);
            }
        };
        this._c2p = c2p.createConverter(clientOptions.uriConverters ? clientOptions.uriConverters.code2Protocol : undefined);
        this._p2c = p2c.createConverter(clientOptions.uriConverters ? clientOptions.uriConverters.protocol2Code : undefined);
    }
    Object.defineProperty(LanguageClient.prototype, "state", {
        get: function () {
            return this._state;
        },
        set: function (value) {
            var oldState = this.getPublicState();
            this._state = value;
            var newState = this.getPublicState();
            if (newState !== oldState) {
                this._stateChangeEmitter.fire({ oldState: oldState, newState: newState });
            }
        },
        enumerable: true,
        configurable: true
    });
    LanguageClient.prototype.getPublicState = function () {
        if (this.state === ClientState.Running) {
            return State.Running;
        }
        else {
            return State.Stopped;
        }
    };
    LanguageClient.prototype.computeSyncExpression = function () {
        var documentSelector = this._clientOptions.documentSelector;
        var textDocumentFilter = this._clientOptions.synchronize.textDocumentFilter;
        if (!documentSelector && !textDocumentFilter) {
            return new FalseSyncExpression();
        }
        if (textDocumentFilter && !documentSelector) {
            return new FunctionSyncExpression(textDocumentFilter);
        }
        if (!textDocumentFilter && documentSelector) {
            if (is.string(documentSelector)) {
                return new LanguageIdExpression(documentSelector);
            }
            else {
                return new CompositeSyncExpression(documentSelector);
            }
        }
        if (textDocumentFilter && documentSelector) {
            return new CompositeSyncExpression(is.string(documentSelector) ? [documentSelector] : documentSelector, textDocumentFilter);
        }
    };
    LanguageClient.prototype.sendRequest = function (type, params, token) {
        var _this = this;
        return this.onReady().then(function () {
            return _this.resolveConnection().then(function (connection) {
                return _this.doSendRequest(connection, type, params, token);
            });
        });
    };
    LanguageClient.prototype.doSendRequest = function (connection, type, params, token) {
        if (this.isConnectionActive()) {
            this.forceDocumentSync();
            try {
                return connection.sendRequest(type, params, token);
            }
            catch (error) {
                this.error("Sending request " + type.method + " failed.", error);
            }
        }
        else {
            return Promise.reject(new vscode_jsonrpc_1.ResponseError(vscode_jsonrpc_1.ErrorCodes.InternalError, 'Connection is closed.'));
        }
    };
    LanguageClient.prototype.sendNotification = function (type, params) {
        var _this = this;
        this.onReady().then(function () {
            _this.resolveConnection().then(function (connection) {
                if (_this.isConnectionActive()) {
                    _this.forceDocumentSync();
                    try {
                        connection.sendNotification(type, params);
                    }
                    catch (error) {
                        _this.error("Sending notification " + type.method + " failed.", error);
                    }
                }
            });
        }, function (error) {
            _this.error("Sending notification " + type.method + " failed.", error);
        });
    };
    LanguageClient.prototype.onNotification = function (type, handler) {
        var _this = this;
        this.onReady().then(function () {
            _this.resolveConnection().then(function (connection) {
                try {
                    connection.onNotification(type, handler);
                }
                catch (error) {
                    _this.error("Registering notification handler " + type.method + " failed.", error);
                }
            });
        }, function (error) {
        });
    };
    LanguageClient.prototype.onRequest = function (type, handler) {
        var _this = this;
        this.onReady().then(function () {
            _this.resolveConnection().then(function (connection) {
                try {
                    connection.onRequest(type, handler);
                }
                catch (error) {
                    _this.error("Registering request handler " + type.method + " failed.", error);
                }
            });
        }, function (error) {
        });
    };
    Object.defineProperty(LanguageClient.prototype, "onTelemetry", {
        get: function () {
            return this._telemetryEmitter.event;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(LanguageClient.prototype, "onDidChangeState", {
        get: function () {
            return this._stateChangeEmitter.event;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(LanguageClient.prototype, "outputChannel", {
        get: function () {
            if (!this._outputChannel) {
                this._outputChannel = vscode_1.window.createOutputChannel(this._clientOptions.outputChannelName ? this._clientOptions.outputChannelName : this._name);
            }
            return this._outputChannel;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(LanguageClient.prototype, "diagnostics", {
        get: function () {
            return this._diagnostics;
        },
        enumerable: true,
        configurable: true
    });
    LanguageClient.prototype.createDefaultErrorHandler = function () {
        return new DefaultErrorHandler(this._name);
    };
    Object.defineProperty(LanguageClient.prototype, "trace", {
        set: function (value) {
            var _this = this;
            this._trace = value;
            this.onReady().then(function () {
                _this.resolveConnection().then(function (connection) {
                    connection.trace(value, _this._tracer);
                });
            }, function (error) {
            });
        },
        enumerable: true,
        configurable: true
    });
    LanguageClient.prototype.data2String = function (data) {
        if (data instanceof vscode_jsonrpc_1.ResponseError) {
            var responseError = data;
            return "  Message: " + responseError.message + "\n  Code: " + responseError.code + " " + (responseError.data ? '\n' + responseError.data.toString() : '');
        }
        if (data instanceof Error) {
            if (is.string(data.stack)) {
                return data.stack;
            }
            return data.message;
        }
        if (is.string(data)) {
            return data;
        }
        return data.toString();
    };
    LanguageClient.prototype.info = function (message, data) {
        this.outputChannel.appendLine("[Info  - " + (new Date().toLocaleTimeString()) + "] " + message);
        if (data) {
            this.outputChannel.appendLine(this.data2String(data));
        }
    };
    LanguageClient.prototype.warn = function (message, data) {
        this.outputChannel.appendLine("[Warn  - " + (new Date().toLocaleTimeString()) + "] " + message);
        if (data) {
            this.outputChannel.appendLine(this.data2String(data));
        }
    };
    LanguageClient.prototype.error = function (message, data) {
        this.outputChannel.appendLine("[Error - " + (new Date().toLocaleTimeString()) + "] " + message);
        if (data) {
            this.outputChannel.appendLine(this.data2String(data));
        }
        this.outputChannel.show();
    };
    LanguageClient.prototype.logTrace = function (message, data) {
        this.outputChannel.appendLine("[Trace - " + (new Date().toLocaleTimeString()) + "] " + message);
        if (data) {
            this.outputChannel.appendLine(this.data2String(data));
        }
        this.outputChannel.show();
    };
    LanguageClient.prototype.needsStart = function () {
        return this.state === ClientState.Initial || this.state === ClientState.Stopping || this.state === ClientState.Stopped;
    };
    LanguageClient.prototype.needsStop = function () {
        return this.state === ClientState.Starting || this.state === ClientState.Running;
    };
    LanguageClient.prototype.onReady = function () {
        return this._onReady;
    };
    LanguageClient.prototype.isConnectionActive = function () {
        return this.state === ClientState.Running;
    };
    LanguageClient.prototype.start = function () {
        var _this = this;
        this._listeners = [];
        this._providers = [];
        // If we restart then the diagnostics collection is reused.
        if (!this._diagnostics) {
            this._diagnostics = this._clientOptions.diagnosticCollectionName
                ? vscode_1.languages.createDiagnosticCollection(this._clientOptions.diagnosticCollectionName)
                : vscode_1.languages.createDiagnosticCollection();
        }
        this.state = ClientState.Starting;
        this.resolveConnection().then(function (connection) {
            connection.onLogMessage(function (message) {
                switch (message.type) {
                    case protocol_1.MessageType.Error:
                        _this.error(message.message);
                        break;
                    case protocol_1.MessageType.Warning:
                        _this.warn(message.message);
                        break;
                    case protocol_1.MessageType.Info:
                        _this.info(message.message);
                        break;
                    default:
                        _this.outputChannel.appendLine(message.message);
                }
            });
            connection.onShowMessage(function (message) {
                switch (message.type) {
                    case protocol_1.MessageType.Error:
                        vscode_1.window.showErrorMessage(message.message);
                        break;
                    case protocol_1.MessageType.Warning:
                        vscode_1.window.showWarningMessage(message.message);
                        break;
                    case protocol_1.MessageType.Info:
                        vscode_1.window.showInformationMessage(message.message);
                        break;
                    default:
                        vscode_1.window.showInformationMessage(message.message);
                }
            });
            connection.onRequest(protocol_1.ShowMessageRequest.type, function (params) {
                var messageFunc = null;
                switch (params.type) {
                    case protocol_1.MessageType.Error:
                        messageFunc = vscode_1.window.showErrorMessage;
                        break;
                    case protocol_1.MessageType.Warning:
                        messageFunc = vscode_1.window.showWarningMessage;
                        break;
                    case protocol_1.MessageType.Info:
                        messageFunc = vscode_1.window.showInformationMessage;
                        break;
                    default:
                        messageFunc = vscode_1.window.showInformationMessage;
                }
                return messageFunc.apply(void 0, [params.message].concat(params.actions));
            });
            connection.onTelemetry(function (data) {
                _this._telemetryEmitter.fire(data);
            });
            connection.listen();
            // Error is handled in the intialize call.
            _this.initialize(connection).then(null, function (error) { });
        }, function (error) {
            _this.state = ClientState.StartFailed;
            _this._onReadyCallbacks.reject(error);
            _this.error('Starting client failed', error);
            vscode_1.window.showErrorMessage("Couldn't start client " + _this._name);
        });
        return new vscode_1.Disposable(function () {
            if (_this.needsStop()) {
                _this.stop();
            }
        });
    };
    LanguageClient.prototype.resolveConnection = function () {
        if (!this._connection) {
            this._connection = this.createConnection();
        }
        return this._connection;
    };
    LanguageClient.prototype.initialize = function (connection) {
        var _this = this;
        this.refreshTrace(connection, false);
        var initOption = this._clientOptions.initializationOptions;
        var initParams = {
            processId: process.pid,
            rootPath: vscode_1.workspace.rootPath,
            capabilities: {},
            initializationOptions: is.func(initOption) ? initOption() : initOption,
            trace: vscode_jsonrpc_1.Trace.toString(this._trace)
        };
        return connection.initialize(initParams).then(function (result) {
            _this.state = ClientState.Running;
            _this._capabilites = result.capabilities;
            connection.onDiagnostics(function (params) { return _this.handleDiagnostics(params); });
            if (_this._capabilites.textDocumentSync !== protocol_1.TextDocumentSyncKind.None) {
                vscode_1.workspace.onDidOpenTextDocument(function (t) { return _this.onDidOpenTextDoument(connection, t); }, null, _this._listeners);
                vscode_1.workspace.onDidChangeTextDocument(function (t) { return _this.onDidChangeTextDocument(connection, t); }, null, _this._listeners);
                vscode_1.workspace.onDidCloseTextDocument(function (t) { return _this.onDidCloseTextDoument(connection, t); }, null, _this._listeners);
                vscode_1.workspace.onDidSaveTextDocument(function (t) { return _this.onDidSaveTextDocument(connection, t); }, null, _this._listeners);
                if (_this._capabilites.textDocumentSync === protocol_1.TextDocumentSyncKind.Full) {
                    _this._documentSyncDelayer = new async_1.Delayer(100);
                }
            }
            _this.hookFileEvents(connection);
            _this.hookConfigurationChanged(connection);
            _this.hookCapabilities(connection);
            _this._onReadyCallbacks.resolve();
            vscode_1.workspace.textDocuments.forEach(function (t) { return _this.onDidOpenTextDoument(connection, t); });
            return result;
        }, function (error) {
            if (_this._clientOptions.initializationFailedHandler) {
                if (_this._clientOptions.initializationFailedHandler(error)) {
                    _this.initialize(connection);
                }
                else {
                    _this.stop();
                    _this._onReadyCallbacks.reject(error);
                }
            }
            else if (error instanceof vscode_jsonrpc_1.ResponseError && error.data && error.data.retry) {
                vscode_1.window.showErrorMessage(error.message, { title: 'Retry', id: "retry" }).then(function (item) {
                    if (is.defined(item) && item.id === 'retry') {
                        _this.initialize(connection);
                    }
                    else {
                        _this.stop();
                        _this._onReadyCallbacks.reject(error);
                    }
                });
            }
            else {
                if (error && error.message) {
                    vscode_1.window.showErrorMessage(error.message);
                }
                _this.error('Server initialization failed.', error);
                _this.stop();
                _this._onReadyCallbacks.reject(error);
            }
        });
    };
    LanguageClient.prototype.stop = function () {
        var _this = this;
        if (!this._connection) {
            this.state = ClientState.Stopped;
            return;
        }
        this.state = ClientState.Stopping;
        this.cleanUp();
        // unkook listeners
        this.resolveConnection().then(function (connection) {
            connection.shutdown().then(function () {
                connection.exit();
                connection.dispose();
                _this.state = ClientState.Stopped;
                _this._connection = null;
                var toCheck = _this._childProcess;
                _this._childProcess = null;
                // Remove all markers
                _this.checkProcessDied(toCheck);
            });
        });
    };
    LanguageClient.prototype.cleanUp = function (diagnostics) {
        if (diagnostics === void 0) { diagnostics = true; }
        if (this._listeners) {
            this._listeners.forEach(function (listener) { return listener.dispose(); });
            this._listeners = null;
        }
        if (this._providers) {
            this._providers.forEach(function (provider) { return provider.dispose(); });
            this._providers = null;
        }
        if (diagnostics) {
            this._diagnostics.dispose();
            this._diagnostics = null;
        }
    };
    LanguageClient.prototype.notifyConfigurationChanged = function (settings) {
        var _this = this;
        this.onReady().then(function () {
            _this.resolveConnection().then(function (connection) {
                if (_this.isConnectionActive()) {
                    connection.didChangeConfiguration({ settings: settings });
                }
            }, function (error) {
                _this.error("Syncing settings failed.", JSON.stringify(error, null, 4));
            });
        }, function (error) {
            _this.error("Syncing settings failed.", JSON.stringify(error, null, 4));
        });
    };
    LanguageClient.prototype.notifyFileEvent = function (event) {
        var _this = this;
        this._fileEvents.push(event);
        this._fileEventDelayer.trigger(function () {
            _this.onReady().then(function () {
                _this.resolveConnection().then(function (connection) {
                    if (_this.isConnectionActive()) {
                        connection.didChangeWatchedFiles({ changes: _this._fileEvents });
                    }
                    _this._fileEvents = [];
                });
            }, function (error) {
                _this.error("Notify file events failed.", error);
            });
        });
    };
    LanguageClient.prototype.onDidOpenTextDoument = function (connection, textDocument) {
        if (!this._syncExpression.evaluate(textDocument)) {
            return;
        }
        connection.didOpenTextDocument(this._c2p.asOpenTextDocumentParams(textDocument));
    };
    LanguageClient.prototype.onDidChangeTextDocument = function (connection, event) {
        var _this = this;
        if (!this._syncExpression.evaluate(event.document)) {
            return;
        }
        var uri = event.document.uri.toString();
        if (this._capabilites.textDocumentSync === protocol_1.TextDocumentSyncKind.Incremental) {
            connection.didChangeTextDocument(this._c2p.asChangeTextDocumentParams(event));
        }
        else {
            this._documentSyncDelayer.trigger(function () {
                connection.didChangeTextDocument(_this._c2p.asChangeTextDocumentParams(event.document));
            }, -1);
        }
    };
    LanguageClient.prototype.onDidCloseTextDoument = function (connection, textDocument) {
        if (!this._syncExpression.evaluate(textDocument)) {
            return;
        }
        connection.didCloseTextDocument(this._c2p.asCloseTextDocumentParams(textDocument));
    };
    LanguageClient.prototype.onDidSaveTextDocument = function (conneciton, textDocument) {
        if (!this._syncExpression.evaluate(textDocument)) {
            return;
        }
        conneciton.didSaveTextDocument(this._c2p.asSaveTextDocumentParams(textDocument));
    };
    LanguageClient.prototype.forceDocumentSync = function () {
        if (this._documentSyncDelayer) {
            this._documentSyncDelayer.forceDelivery();
        }
    };
    LanguageClient.prototype.handleDiagnostics = function (params) {
        var uri = vscode_1.Uri.parse(params.uri);
        var diagnostics = this._p2c.asDiagnostics(params.diagnostics);
        this._diagnostics.set(uri, diagnostics);
    };
    LanguageClient.prototype.createConnection = function () {
        var _this = this;
        function getEnvironment(env) {
            if (!env) {
                return process.env;
            }
            var result = Object.create(null);
            Object.keys(process.env).forEach(function (key) { return result[key] = process.env[key]; });
            Object.keys(env).forEach(function (key) { return result[key] = env[key]; });
        }
        var encoding = this._clientOptions.stdioEncoding || 'utf8';
        var errorHandler = function (error, message, count) {
            _this.handleConnectionError(error, message, count);
        };
        var closeHandler = function () {
            _this.handleConnectionClosed();
        };
        var server = this._serverOptions;
        // We got a function.
        if (is.func(server)) {
            return server().then(function (result) {
                var info = result;
                if (info.writer && info.reader) {
                    return createConnection(info.reader, info.writer, errorHandler, closeHandler);
                }
                else {
                    var cp_1 = result;
                    return createConnection(cp_1.stdout, cp_1.stdin, errorHandler, closeHandler);
                }
            });
        }
        var json = null;
        var runDebug = server;
        if (is.defined(runDebug.run) || is.defined(runDebug.debug)) {
            // We are under debugging. So use debug as well.
            if (typeof v8debug === 'object' || this._forceDebug) {
                json = runDebug.debug;
            }
            else {
                json = runDebug.run;
            }
        }
        else {
            json = server;
        }
        if (is.defined(json.module)) {
            var node_1 = json;
            if (node_1.runtime) {
                var args_1 = [];
                var options = node_1.options || Object.create(null);
                if (options.execArgv) {
                    options.execArgv.forEach(function (element) { return args_1.push(element); });
                }
                args_1.push(node_1.module);
                if (node_1.args) {
                    node_1.args.forEach(function (element) { return args_1.push(element); });
                }
                var execOptions = Object.create(null);
                execOptions.cwd = options.cwd || vscode_1.workspace.rootPath;
                execOptions.env = getEnvironment(options.env);
                if (node_1.transport === TransportKind.ipc) {
                    execOptions.stdio = [null, null, null, 'ipc'];
                    args_1.push('--node-ipc');
                }
                else if (node_1.transport === TransportKind.stdio) {
                    args_1.push('--stdio');
                }
                var process_1 = cp.spawn(node_1.runtime, args_1, execOptions);
                if (!process_1 || !process_1.pid) {
                    return Promise.reject("Launching server using runtime " + node_1.runtime + " failed.");
                }
                this._childProcess = process_1;
                process_1.stderr.on('data', function (data) { return _this.outputChannel.append(data.toString(encoding)); });
                if (node_1.transport === TransportKind.ipc) {
                    process_1.stdout.on('data', function (data) { return _this.outputChannel.append(data.toString(encoding)); });
                    return Promise.resolve(createConnection(new vscode_jsonrpc_1.IPCMessageReader(process_1), new vscode_jsonrpc_1.IPCMessageWriter(process_1), errorHandler, closeHandler));
                }
                else {
                    return Promise.resolve(createConnection(process_1.stdout, process_1.stdin, errorHandler, closeHandler));
                }
            }
            else {
                return new Promise(function (resolve, reject) {
                    var args = node_1.args && node_1.args.slice() || [];
                    if (node_1.transport === TransportKind.ipc) {
                        args.push('--node-ipc');
                    }
                    else if (node_1.transport === TransportKind.stdio) {
                        args.push('--stdio');
                    }
                    var options = node_1.options || Object.create(null);
                    options.execArgv = options.execArgv || [];
                    options.cwd = options.cwd || vscode_1.workspace.rootPath;
                    electron.fork(node_1.module, args || [], options, function (error, cp) {
                        if (error) {
                            reject(error);
                        }
                        else {
                            _this._childProcess = cp;
                            cp.stderr.on('data', function (data) { return _this.outputChannel.append(data.toString(encoding)); });
                            if (node_1.transport === TransportKind.ipc) {
                                cp.stdout.on('data', function (data) { return _this.outputChannel.append(data.toString(encoding)); });
                                resolve(createConnection(new vscode_jsonrpc_1.IPCMessageReader(_this._childProcess), new vscode_jsonrpc_1.IPCMessageWriter(_this._childProcess), errorHandler, closeHandler));
                            }
                            else {
                                resolve(createConnection(cp.stdout, cp.stdin, errorHandler, closeHandler));
                            }
                        }
                    });
                });
            }
        }
        else if (is.defined(json.command)) {
            var command = json;
            var options = command.options || {};
            options.cwd = options.cwd || vscode_1.workspace.rootPath;
            var process_2 = cp.spawn(command.command, command.args, command.options);
            if (!process_2 || !process_2.pid) {
                return Promise.reject("Launching server using command " + command.command + " failed.");
            }
            process_2.stderr.on('data', function (data) { return _this.outputChannel.append(data.toString(encoding)); });
            this._childProcess = process_2;
            return Promise.resolve(createConnection(process_2.stdout, process_2.stdin, errorHandler, closeHandler));
        }
        return Promise.reject(new Error("Unsupported server configuartion " + JSON.stringify(server, null, 4)));
    };
    LanguageClient.prototype.handleConnectionClosed = function () {
        // Check whether this is a normal shutdown in progress or the client stopped normally.
        if (this.state === ClientState.Stopping || this.state === ClientState.Stopped) {
            return;
        }
        this._connection = null;
        this._childProcess = null;
        var action = this._clientOptions.errorHandler.closed();
        if (action === CloseAction.DoNotRestart) {
            this.error('Connection to server got closed. Server will not be restarted.');
            this.state = ClientState.Stopped;
            this.cleanUp();
        }
        else if (action === CloseAction.Restart && this.state !== ClientState.Stopping) {
            this.info('Connection to server got closed. Server will restart.');
            this.cleanUp(false);
            this.state = ClientState.Initial;
            this.start();
        }
    };
    LanguageClient.prototype.handleConnectionError = function (error, message, count) {
        var action = this._clientOptions.errorHandler.error(error, message, count);
        if (action === ErrorAction.Shutdown) {
            this.error('Connection to server is erroring. Shutting down server.');
            this.stop();
        }
    };
    LanguageClient.prototype.checkProcessDied = function (childProcess) {
        if (!childProcess) {
            return;
        }
        setTimeout(function () {
            // Test if the process is still alive. Throws an exception if not
            try {
                process.kill(childProcess.pid, 0);
                processes_1.terminate(childProcess);
            }
            catch (error) {
            }
        }, 2000);
    };
    LanguageClient.prototype.hookConfigurationChanged = function (connection) {
        var _this = this;
        if (!this._clientOptions.synchronize.configurationSection) {
            return;
        }
        vscode_1.workspace.onDidChangeConfiguration(function (e) { return _this.onDidChangeConfiguration(connection); }, this, this._listeners);
        this.onDidChangeConfiguration(connection);
    };
    LanguageClient.prototype.refreshTrace = function (connection, sendNotification) {
        if (sendNotification === void 0) { sendNotification = false; }
        var config = vscode_1.workspace.getConfiguration(this._id);
        var trace = vscode_jsonrpc_1.Trace.Off;
        if (config) {
            trace = vscode_jsonrpc_1.Trace.fromString(config.get('trace.server', 'off'));
        }
        this._trace = trace;
        connection.trace(this._trace, this._tracer, sendNotification);
    };
    LanguageClient.prototype.onDidChangeConfiguration = function (connection) {
        this.refreshTrace(connection, true);
        var keys = null;
        var configurationSection = this._clientOptions.synchronize.configurationSection;
        if (is.string(configurationSection)) {
            keys = [configurationSection];
        }
        else if (is.stringArray(configurationSection)) {
            keys = configurationSection;
        }
        if (keys) {
            if (this.isConnectionActive()) {
                connection.didChangeConfiguration({ settings: this.extractSettingsInformation(keys) });
            }
        }
    };
    LanguageClient.prototype.extractSettingsInformation = function (keys) {
        function ensurePath(config, path) {
            var current = config;
            for (var i = 0; i < path.length - 1; i++) {
                var obj = current[path[i]];
                if (!obj) {
                    obj = Object.create(null);
                    current[path[i]] = obj;
                }
                current = obj;
            }
            return current;
        }
        var result = Object.create(null);
        for (var i = 0; i < keys.length; i++) {
            var key = keys[i];
            var index = key.indexOf('.');
            var config = null;
            if (index >= 0) {
                config = vscode_1.workspace.getConfiguration(key.substr(0, index)).get(key.substr(index + 1));
            }
            else {
                config = vscode_1.workspace.getConfiguration(key);
            }
            if (config) {
                var path = keys[i].split('.');
                ensurePath(result, path)[path[path.length - 1]] = config;
            }
        }
        return result;
    };
    LanguageClient.prototype.hookFileEvents = function (connection) {
        var _this = this;
        var fileEvents = this._clientOptions.synchronize.fileEvents;
        if (!fileEvents) {
            return;
        }
        var watchers = null;
        if (is.array(fileEvents)) {
            watchers = fileEvents;
        }
        else {
            watchers = [fileEvents];
        }
        if (!watchers) {
            return;
        }
        watchers.forEach(function (watcher) {
            watcher.onDidCreate(function (resource) { return _this.notifyFileEvent({
                uri: resource.toString(),
                type: protocol_1.FileChangeType.Created
            }); }, null, _this._listeners);
            watcher.onDidChange(function (resource) { return _this.notifyFileEvent({
                uri: resource.toString(),
                type: protocol_1.FileChangeType.Changed
            }); }, null, _this._listeners);
            watcher.onDidDelete(function (resource) { return _this.notifyFileEvent({
                uri: resource.toString(),
                type: protocol_1.FileChangeType.Deleted
            }); }, null, _this._listeners);
        });
    };
    LanguageClient.prototype.hookCapabilities = function (connection) {
        var documentSelector = this._clientOptions.documentSelector;
        if (!documentSelector) {
            return;
        }
        this.hookCompletionProvider(documentSelector, connection);
        this.hookHoverProvider(documentSelector, connection);
        this.hookSignatureHelpProvider(documentSelector, connection);
        this.hookDefinitionProvider(documentSelector, connection);
        this.hookReferencesProvider(documentSelector, connection);
        this.hookDocumentHighlightProvider(documentSelector, connection);
        this.hookDocumentSymbolProvider(documentSelector, connection);
        this.hookWorkspaceSymbolProvider(connection);
        this.hookCodeActionsProvider(documentSelector, connection);
        this.hookCodeLensProvider(documentSelector, connection);
        this.hookDocumentFormattingProvider(documentSelector, connection);
        this.hookDocumentRangeFormattingProvider(documentSelector, connection);
        this.hookDocumentOnTypeFormattingProvider(documentSelector, connection);
        this.hookRenameProvider(documentSelector, connection);
    };
    LanguageClient.prototype.logFailedRequest = function (type, error) {
        this.error("Request " + type.method + " failed.", error);
    };
    LanguageClient.prototype.hookCompletionProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.completionProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerCompletionItemProvider.apply(vscode_1.languages, [documentSelector, {
            provideCompletionItems: function (document, position, token) {
                return _this.doSendRequest(connection, protocol_1.CompletionRequest.type, _this._c2p.asTextDocumentPositionParams(document, position), token).then(_this._p2c.asCompletionResult, function (error) {
                    _this.logFailedRequest(protocol_1.CompletionRequest.type, error);
                    return Promise.resolve([]);
                });
            },
            resolveCompletionItem: this._capabilites.completionProvider.resolveProvider
                ? function (item, token) {
                    return _this.doSendRequest(connection, protocol_1.CompletionResolveRequest.type, _this._c2p.asCompletionItem(item), token).then(_this._p2c.asCompletionItem, function (error) {
                        _this.logFailedRequest(protocol_1.CompletionResolveRequest.type, error);
                        return Promise.resolve(item);
                    });
                }
                : undefined
        }].concat(this._capabilites.completionProvider.triggerCharacters)));
    };
    LanguageClient.prototype.hookHoverProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.hoverProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerHoverProvider(documentSelector, {
            provideHover: function (document, position, token) {
                return _this.doSendRequest(connection, protocol_1.HoverRequest.type, _this._c2p.asTextDocumentPositionParams(document, position), token).then(_this._p2c.asHover, function (error) {
                    _this.logFailedRequest(protocol_1.HoverRequest.type, error);
                    return Promise.resolve(null);
                });
            }
        }));
    };
    LanguageClient.prototype.hookSignatureHelpProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.signatureHelpProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerSignatureHelpProvider.apply(vscode_1.languages, [documentSelector, {
            provideSignatureHelp: function (document, position, token) {
                return _this.doSendRequest(connection, protocol_1.SignatureHelpRequest.type, _this._c2p.asTextDocumentPositionParams(document, position), token).then(_this._p2c.asSignatureHelp, function (error) {
                    _this.logFailedRequest(protocol_1.SignatureHelpRequest.type, error);
                    return Promise.resolve(null);
                });
            }
        }].concat(this._capabilites.signatureHelpProvider.triggerCharacters)));
    };
    LanguageClient.prototype.hookDefinitionProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.definitionProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerDefinitionProvider(documentSelector, {
            provideDefinition: function (document, position, token) {
                return _this.doSendRequest(connection, protocol_1.DefinitionRequest.type, _this._c2p.asTextDocumentPositionParams(document, position), token).then(_this._p2c.asDefinitionResult, function (error) {
                    _this.logFailedRequest(protocol_1.DefinitionRequest.type, error);
                    return Promise.resolve(null);
                });
            }
        }));
    };
    LanguageClient.prototype.hookReferencesProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.referencesProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerReferenceProvider(documentSelector, {
            provideReferences: function (document, position, options, token) {
                return _this.doSendRequest(connection, protocol_1.ReferencesRequest.type, _this._c2p.asReferenceParams(document, position, options), token).then(_this._p2c.asReferences, function (error) {
                    _this.logFailedRequest(protocol_1.ReferencesRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }));
    };
    LanguageClient.prototype.hookDocumentHighlightProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.documentHighlightProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerDocumentHighlightProvider(documentSelector, {
            provideDocumentHighlights: function (document, position, token) {
                return _this.doSendRequest(connection, protocol_1.DocumentHighlightRequest.type, _this._c2p.asTextDocumentPositionParams(document, position), token).then(_this._p2c.asDocumentHighlights, function (error) {
                    _this.logFailedRequest(protocol_1.DocumentHighlightRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }));
    };
    LanguageClient.prototype.hookDocumentSymbolProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.documentSymbolProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerDocumentSymbolProvider(documentSelector, {
            provideDocumentSymbols: function (document, token) {
                return _this.doSendRequest(connection, protocol_1.DocumentSymbolRequest.type, _this._c2p.asDocumentSymbolParams(document), token).then(_this._p2c.asSymbolInformations, function (error) {
                    _this.logFailedRequest(protocol_1.DocumentSymbolRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }));
    };
    LanguageClient.prototype.hookWorkspaceSymbolProvider = function (connection) {
        var _this = this;
        if (!this._capabilites.workspaceSymbolProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerWorkspaceSymbolProvider({
            provideWorkspaceSymbols: function (query, token) {
                return _this.doSendRequest(connection, protocol_1.WorkspaceSymbolRequest.type, { query: query }, token).then(_this._p2c.asSymbolInformations, function (error) {
                    _this.logFailedRequest(protocol_1.WorkspaceSymbolRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }));
    };
    LanguageClient.prototype.hookCodeActionsProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.codeActionProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerCodeActionsProvider(documentSelector, {
            provideCodeActions: function (document, range, context, token) {
                var params = {
                    textDocument: _this._c2p.asTextDocumentIdentifier(document),
                    range: _this._c2p.asRange(range),
                    context: _this._c2p.asCodeActionContext(context)
                };
                return _this.doSendRequest(connection, protocol_1.CodeActionRequest.type, params, token).then(_this._p2c.asCommands, function (error) {
                    _this.logFailedRequest(protocol_1.CodeActionRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }));
    };
    LanguageClient.prototype.hookCodeLensProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.codeLensProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerCodeLensProvider(documentSelector, {
            provideCodeLenses: function (document, token) {
                return _this.doSendRequest(connection, protocol_1.CodeLensRequest.type, _this._c2p.asCodeLensParams(document), token).then(_this._p2c.asCodeLenses, function (error) {
                    _this.logFailedRequest(protocol_1.CodeLensRequest.type, error);
                    return Promise.resolve([]);
                });
            },
            resolveCodeLens: (this._capabilites.codeLensProvider.resolveProvider)
                ? function (codeLens, token) {
                    return _this.doSendRequest(connection, protocol_1.CodeLensResolveRequest.type, _this._c2p.asCodeLens(codeLens), token).then(_this._p2c.asCodeLens, function (error) {
                        _this.logFailedRequest(protocol_1.CodeLensResolveRequest.type, error);
                        return codeLens;
                    });
                }
                : undefined
        }));
    };
    LanguageClient.prototype.hookDocumentFormattingProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.documentFormattingProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerDocumentFormattingEditProvider(documentSelector, {
            provideDocumentFormattingEdits: function (document, options, token) {
                var params = {
                    textDocument: _this._c2p.asTextDocumentIdentifier(document),
                    options: _this._c2p.asFormattingOptions(options)
                };
                return _this.doSendRequest(connection, protocol_1.DocumentFormattingRequest.type, params, token).then(_this._p2c.asTextEdits, function (error) {
                    _this.logFailedRequest(protocol_1.DocumentFormattingRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }));
    };
    LanguageClient.prototype.hookDocumentRangeFormattingProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.documentRangeFormattingProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerDocumentRangeFormattingEditProvider(documentSelector, {
            provideDocumentRangeFormattingEdits: function (document, range, options, token) {
                var params = {
                    textDocument: _this._c2p.asTextDocumentIdentifier(document),
                    range: _this._c2p.asRange(range),
                    options: _this._c2p.asFormattingOptions(options)
                };
                return _this.doSendRequest(connection, protocol_1.DocumentRangeFormattingRequest.type, params, token).then(_this._p2c.asTextEdits, function (error) {
                    _this.logFailedRequest(protocol_1.DocumentRangeFormattingRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }));
    };
    LanguageClient.prototype.hookDocumentOnTypeFormattingProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.documentOnTypeFormattingProvider) {
            return;
        }
        var formatCapabilities = this._capabilites.documentOnTypeFormattingProvider;
        this._providers.push(vscode_1.languages.registerOnTypeFormattingEditProvider.apply(vscode_1.languages, [documentSelector, {
            provideOnTypeFormattingEdits: function (document, position, ch, options, token) {
                var params = {
                    textDocument: _this._c2p.asTextDocumentIdentifier(document),
                    position: _this._c2p.asPosition(position),
                    ch: ch,
                    options: _this._c2p.asFormattingOptions(options)
                };
                return _this.doSendRequest(connection, protocol_1.DocumentOnTypeFormattingRequest.type, params, token).then(_this._p2c.asTextEdits, function (error) {
                    _this.logFailedRequest(protocol_1.DocumentOnTypeFormattingRequest.type, error);
                    return Promise.resolve([]);
                });
            }
        }, formatCapabilities.firstTriggerCharacter].concat(formatCapabilities.moreTriggerCharacter)));
    };
    LanguageClient.prototype.hookRenameProvider = function (documentSelector, connection) {
        var _this = this;
        if (!this._capabilites.renameProvider) {
            return;
        }
        this._providers.push(vscode_1.languages.registerRenameProvider(documentSelector, {
            provideRenameEdits: function (document, position, newName, token) {
                var params = {
                    textDocument: _this._c2p.asTextDocumentIdentifier(document),
                    position: _this._c2p.asPosition(position),
                    newName: newName
                };
                return _this.doSendRequest(connection, protocol_1.RenameRequest.type, params, token).then(_this._p2c.asWorkspaceEdit, function (error) {
                    _this.logFailedRequest(protocol_1.RenameRequest.type, error);
                    Promise.resolve(new Error(error.message));
                });
            }
        }));
    };
    return LanguageClient;
}());
exports.LanguageClient = LanguageClient;
var SettingMonitor = (function () {
    function SettingMonitor(_client, _setting) {
        this._client = _client;
        this._setting = _setting;
        this._listeners = [];
    }
    SettingMonitor.prototype.start = function () {
        var _this = this;
        vscode_1.workspace.onDidChangeConfiguration(this.onDidChangeConfiguration, this, this._listeners);
        this.onDidChangeConfiguration();
        return new vscode_1.Disposable(function () {
            if (_this._client.needsStop()) {
                _this._client.stop();
            }
        });
    };
    SettingMonitor.prototype.onDidChangeConfiguration = function () {
        var index = this._setting.indexOf('.');
        var primary = index >= 0 ? this._setting.substr(0, index) : this._setting;
        var rest = index >= 0 ? this._setting.substr(index + 1) : undefined;
        var enabled = rest ? vscode_1.workspace.getConfiguration(primary).get(rest, false) : vscode_1.workspace.getConfiguration(primary);
        if (enabled && this._client.needsStart()) {
            this._client.start();
        }
        else if (!enabled && this._client.needsStop()) {
            this._client.stop();
        }
    };
    return SettingMonitor;
}());
exports.SettingMonitor = SettingMonitor;
