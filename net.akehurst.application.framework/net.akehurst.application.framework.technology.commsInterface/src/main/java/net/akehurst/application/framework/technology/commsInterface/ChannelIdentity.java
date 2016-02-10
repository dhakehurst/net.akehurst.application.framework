package net.akehurst.application.framework.technology.commsInterface;

import net.akehurst.application.framework.common.AbstractDataType;

public class ChannelIdentity extends AbstractDataType {

	public ChannelIdentity(String value) {
		super(value);
		this.value = value;
	}

	String value;
	public String getValue() {
		return this.value;
	}
	
}
