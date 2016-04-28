package net.akehurst.application.framework.technology.guiInterface;

import net.akehurst.application.framework.common.AbstractDataType;

public class StageIdentity extends AbstractDataType {

	public StageIdentity(String value) {
		super(value);
		this.value = value;
	}
	
	String value;
	public String asPrimitive() {
		return this.value;
	}
}
