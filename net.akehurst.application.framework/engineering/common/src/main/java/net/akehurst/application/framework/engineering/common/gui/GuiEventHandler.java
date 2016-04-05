package net.akehurst.application.framework.engineering.common.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractIdentifiableObject;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;


public class GuiEventHandler extends AbstractIdentifiableObject {

	public GuiEventHandler(String id, ILogger logger) { //TODO:enable logger to be injected!
		super(id);
		this.handlers = new HashMap<>();
		this.logger = logger;
	}

	@ServiceReference
	ILogger logger;
	
	Map<GuiEventSignature, Consumer<GuiEvent> > handlers;
	
	public void register(GuiEventSignature signature, Consumer<GuiEvent> action) {
		this.handlers.put(signature, action);
	}

	public void handle(GuiEvent event) {
		Consumer<GuiEvent> handler = this.handlers.get(event.getSignature());
		if (null==handler) {
			logger.log(LogLevel.WARN, "Unhandled event, "+event.getSignature());
		} else {
			handler.accept(event);
		}
	}
	
	
	
	
	
}
