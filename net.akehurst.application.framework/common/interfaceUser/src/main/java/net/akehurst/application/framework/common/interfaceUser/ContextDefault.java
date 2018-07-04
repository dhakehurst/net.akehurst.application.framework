package net.akehurst.application.framework.common.interfaceUser;

import net.akehurst.application.framework.common.Context;

public class ContextDefault implements Context {

	private final UserSession userSession;

	public ContextDefault(final UserSession userSession) {
		this.userSession = userSession;
	}

}
