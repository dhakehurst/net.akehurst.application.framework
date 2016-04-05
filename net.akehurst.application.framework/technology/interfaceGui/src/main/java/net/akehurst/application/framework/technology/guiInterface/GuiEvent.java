package net.akehurst.application.framework.technology.guiInterface;

import java.util.Map;

import net.akehurst.application.framework.technology.authentication.TechSession;

public class GuiEvent {

	public GuiEvent(TechSession session, GuiEventSignature signature, Map<String, Object> eventData) {
		this.session = session;
		this.signature = signature;
		this.eventData = eventData;
	}
	
	TechSession session;
	public TechSession getTechSession() {
		return session;
	}
	
	GuiEventSignature signature;
	public GuiEventSignature getSignature() {
		return this.signature;
	}
	
	Map<String, Object> eventData;
	public Map<String, Object> getEventData() {
		return this.eventData;
	}
	public Object getDataItem(String key) {
		return this.eventData.get(key);
	}
}
