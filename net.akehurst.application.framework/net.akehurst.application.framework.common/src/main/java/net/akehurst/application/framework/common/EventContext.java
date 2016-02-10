package net.akehurst.application.framework.common;

import java.util.HashMap;
import java.util.Map;

public class EventContext {

	public EventContext() {
		this.data = new HashMap<>();
	}
	
	Map<String, Object> data;
	public Map<String, Object> getData() {
		return this.data;
	}
}
