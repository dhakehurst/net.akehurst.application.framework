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
package net.akehurst.application.framework.technology.gui.jfx.elements;

import javafx.scene.Node;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiText;

public class JfxText extends JfxGuiElement implements IGuiText {

	public JfxText(final Node n) {
		super(n);
	}

	// @Override
	// public String getText(UserSession session) {
	// return (String)super.get(session, "text");
	// }

	@Override
	public void setText(final UserSession session, final String value) {
		super.set(session, "text", value);
	}

}
