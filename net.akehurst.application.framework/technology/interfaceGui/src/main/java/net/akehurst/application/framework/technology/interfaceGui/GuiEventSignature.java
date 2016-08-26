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

import net.akehurst.application.framework.common.AbstractDataType;

public class GuiEventSignature extends AbstractDataType {

	public GuiEventSignature(final StageIdentity stageId, final SceneIdentity sceneId, final String elementId, final String eventType) {
		super(stageId, sceneId, elementId, eventType);
		this.stageId = stageId;
		this.sceneId = sceneId;
		this.elementId = elementId;
		this.eventType = eventType;
	}

	StageIdentity stageId;

	public StageIdentity getStageId() {
		return this.stageId;
	}

	SceneIdentity sceneId;

	public SceneIdentity getSceneId() {
		return this.sceneId;
	}

	String elementId;

	public String getElementId() {
		return this.elementId;
	}

	String eventType;

	public String getEventType() {
		return this.eventType;
	}
}
