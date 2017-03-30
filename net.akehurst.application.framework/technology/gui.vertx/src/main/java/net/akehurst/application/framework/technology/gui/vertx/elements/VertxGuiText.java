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
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

public class VertxGuiText extends VertxGuiElement implements IGuiText {

	public VertxGuiText(final IGuiRequest guiRequest, final IGuiScene scene, final IGuiDialog dialog, final String elementName) {
		super(guiRequest, scene, dialog, elementName);
	}

	@Override
	public void setText(final UserSession session, final String value) {
		this.getGuiRequest().setText(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), value);
	}

	@Override
	public void onTextChange(final UserSession session, final EventTextChange event) {
		super.onEvent(session, "oninput", (e) -> event.execute());
	}

}
