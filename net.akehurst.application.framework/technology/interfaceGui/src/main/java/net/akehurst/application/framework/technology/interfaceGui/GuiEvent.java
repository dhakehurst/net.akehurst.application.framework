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

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public class GuiEvent {

	public GuiEvent(final UserSession session, final GuiEventSignature signature, final Map<String, Object> eventData) {
		this.session = session;
		this.signature = signature;
		this.eventData = eventData;
	}

	UserSession session;

	public UserSession getSession() {
		return this.session;
	}

	GuiEventSignature signature;

	public GuiEventSignature getSignature() {
		return this.signature;
	}

	Map<String, Object> eventData;

	public Map<String, Object> getEventData() {
		return this.eventData;
	}

	public <T> T getDataItem(final String key) {
		return (T) this.eventData.get(key);
	}
}
