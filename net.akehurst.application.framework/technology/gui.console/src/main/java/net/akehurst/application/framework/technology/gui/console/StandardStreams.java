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
package net.akehurst.application.framework.technology.gui.console;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceGui.console.IConsoleNotification;
import net.akehurst.application.framework.technology.interfaceGui.console.IConsoleRequest;

@Component
public class StandardStreams extends AbstractComponent implements IConsoleRequest {

	public StandardStreams(final String id) {
		super(id);
	}

	@Override
	public void afRun() {
		final UserSession session = new UserSession(null, new UserDetails(System.getProperty("user.name")));
		this.portOutput().out(IConsoleNotification.class).notifyReady(session);
	}

	@Override
	public void requestOutput(final String str) {
		System.out.println(str);
	}

	@Override
	public void requestError(final String str) {
		System.err.println(str);
	}

	@PortInstance(provides = { IConsoleRequest.class }, requires = { IConsoleNotification.class })
	IPort portOutput;

	public IPort portOutput() {
		return this.portOutput;
	}
}
