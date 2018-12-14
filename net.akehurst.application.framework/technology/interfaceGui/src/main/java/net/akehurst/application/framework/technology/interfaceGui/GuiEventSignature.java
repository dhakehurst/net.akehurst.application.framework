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
package net.akehurst.application.framework.technology.interfaceGui;

import net.akehurst.datatype.common.DatatypeAbstract;

public class GuiEventSignature extends DatatypeAbstract {

    public GuiEventSignature(final StageIdentity stageId, final SceneIdentity sceneId, final DialogIdentity dialogId, final String elementId,
            final GuiEventType eventType) {
        super(stageId, sceneId, dialogId, elementId, eventType);
        this.stageId = stageId;
        this.sceneId = sceneId;
        this.dialogId = dialogId;
        this.elementId = elementId;
        this.eventType = eventType;
    }

    private final StageIdentity stageId;
    private final SceneIdentity sceneId;
    private final DialogIdentity dialogId;
    private final String elementId;

    public StageIdentity getStageId() {
        return this.stageId;
    }

    public SceneIdentity getSceneId() {
        return this.sceneId;
    }

    public DialogIdentity getDialogId() {
        return this.dialogId;
    }

    public String getElementId() {
        return this.elementId;
    }

    private final GuiEventType eventType;

    public GuiEventType getEventType() {
        return this.eventType;
    }
}
