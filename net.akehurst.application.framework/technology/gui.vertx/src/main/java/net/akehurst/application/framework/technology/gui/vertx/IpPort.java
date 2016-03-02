package net.akehurst.application.framework.technology.gui.vertx;

import net.akehurst.application.framework.common.AbstractDataType;

public class IpPort extends AbstractDataType {
	
	public IpPort(String value) {
		this(Integer.parseInt(value));
	}
	
	public IpPort(Integer value) {
		super(value);
		this.value = value;
	}

	Integer value;

	public Integer asPrimitive() {
		return this.value;
	}

}
