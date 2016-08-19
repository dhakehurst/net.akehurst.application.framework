package net.akehurst.application.framework.technology.gui.common;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractIdentifiableObject;
import net.akehurst.application.framework.technology.guiInterface.GuiEvent;
import net.akehurst.application.framework.technology.guiInterface.GuiEventSignature;
import net.akehurst.application.framework.technology.guiInterface.IGuiScene.OnEventHandler;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class GuiEventHandler extends AbstractIdentifiableObject {

	public GuiEventHandler(final String afId) {
		super(afId);
		this.handlers = new HashMap<>();
	}

	@ServiceReference
	ILogger logger;

	Map<GuiEventSignature, OnEventHandler> handlers;

	public void register(final GuiEventSignature signature, final OnEventHandler handler) {
		this.handlers.put(signature, handler);
	}

	public void handle(final GuiEvent event) {
		final OnEventHandler handler = this.handlers.get(event.getSignature());
		if (null == handler) {
			this.logger.log(LogLevel.WARN, "Unhandled event, " + event.getSignature());
		} else {
			handler.execute(event);
		}
	}

}
