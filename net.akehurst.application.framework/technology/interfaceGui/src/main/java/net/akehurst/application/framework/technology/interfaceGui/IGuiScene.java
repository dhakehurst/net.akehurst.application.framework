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

import java.util.Map;

import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;

public interface IGuiScene extends IIdentifiableObject {

	StageIdentity getStageId();

	SceneIdentity getSceneId();

	/**
	 * called by the framework to indicate that an event has occured
	 *
	 * @param event
	 */
	void notifyEventOccured(GuiEvent event);

	void switchTo(UserSession session);

	void switchTo(UserSession session, Map<String, String> sceneArguments);

	public interface OnEventHandler {
		void execute(GuiEvent event);
	}

	/**
	 * Register an event handler for an event.
	 *
	 * @param session
	 * @param eventSignature
	 * @param handler
	 */
	void onEvent(UserSession session, GuiEventSignature eventSignature, OnEventHandler handler);

	<T extends IGuiDialog> T createDialog(Class<T> dialogClass, UserSession session, DialogIdentity dialogId, String title, String dialogContent);

	/**
	 *
	 * @param elementName
	 *            the id (or class) of an element
	 * @return
	 */
	IGuiElement getElement(String elementName);

	IGuiDialog getDialog(DialogIdentity dialogId);
}
