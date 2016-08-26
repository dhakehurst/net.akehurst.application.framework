package net.akehurst.application.framework.technology.guiInterface.console;

import net.akehurst.application.framework.common.annotations.declaration.Signal;

public interface IConsoleRequest {

	@Signal
	void requestOutput(String str);

	@Signal
	void requestError(String str);
}
