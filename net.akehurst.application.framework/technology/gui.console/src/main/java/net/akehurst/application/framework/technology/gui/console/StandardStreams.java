package net.akehurst.application.framework.technology.gui.console;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.os.AbstractComponent;
import net.akehurst.application.framework.technology.guiInterface.console.IConsoleNotification;
import net.akehurst.application.framework.technology.guiInterface.console.IConsoleRequest;

@Component
public class StandardStreams extends AbstractComponent implements IConsoleRequest {

	public StandardStreams(String id) {
		super(id);
	}

	@Override
	public void afRun() {
		portOutput().out(IConsoleNotification.class).notifyReady();
	}
	
	@Override
	public void requestOutput(String str) {
		System.out.println(str);
	}
	
	@Override
	public void requestError(String str) {
		System.err.println(str);
	}
	
	@PortInstance(provides={IConsoleRequest.class}, requires={IConsoleNotification.class})
	IPort portOutput;
	public IPort portOutput() {
		return this.portOutput;
	}
}
