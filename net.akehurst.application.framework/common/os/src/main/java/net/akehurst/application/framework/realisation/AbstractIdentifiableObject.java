package net.akehurst.application.framework.realisation;

public class AbstractIdentifiableObject {

	public AbstractIdentifiableObject(String id) {
		this.id = id;
	}

	String id;

	public String afId() {
		return this.id;
	}
}
