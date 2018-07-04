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
package net.akehurst.application.framework.technology.gui.common;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.IdentifiableObjectAbstract;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class GuiEventHandler extends IdentifiableObjectAbstract {

    public GuiEventHandler(final String afId) {
        super(afId);
        this.handlers = new HashMap<>();
    }

    @ServiceReference
    ILogger logger;

    Map<GuiEventSignature, OnEventHandler> handlers;

    public void register(final GuiEventSignature signature, final OnEventHandler handler) {
        this.handlers.put(signature, handler);
    }

    public void handle(final GuiEvent event) {
        final OnEventHandler handler = this.handlers.get(event.getSignature());
        if (null == handler) {
            this.logger.log(LogLevel.WARN, "Unhandled event, " + event.getSignature());
        } else {
            handler.execute(event);
        }
    }

}
