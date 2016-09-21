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
package net.akehurst.application.framework.technology.gui.vertx.elements;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.editor.IGuiEditor;
import net.akehurst.application.framework.technology.interfaceGui.data.editor.IGuiLanguageService;

public class VertxGuiEditor extends VertxGuiElement implements IGuiEditor {

	public VertxGuiEditor(final IGuiRequest guiRequest, final IGuiScene scene, final String elementName) {
		super(guiRequest, scene, elementName);
	}

	@Override
	public void add(final UserSession session, final String initialContent, final IGuiLanguageService languageDefinition) {
		super.guiRequest.addEditor(session, this.scene.getStageId(), this.scene.getSceneId(), this.elementName, initialContent, languageDefinition);
	}

}
