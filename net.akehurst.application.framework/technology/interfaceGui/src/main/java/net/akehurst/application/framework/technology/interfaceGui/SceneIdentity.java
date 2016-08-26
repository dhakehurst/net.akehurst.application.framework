package net.akehurst.application.framework.technology.guiInterface;

import net.akehurst.application.framework.common.AbstractDataType;

public class SceneIdentity extends AbstractDataType {

	public SceneIdentity(String value) {
		super(value);
		this.value = value;
	}
	
	String value;
	public String asPrimitive() {
		return this.value;
	}
}
