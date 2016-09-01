package net.akehurst.application.framework.common;

abstract public class AbstractService implements IService {

	public AbstractService(final String afId) {
		this.afId = afId;
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	@Override
	public Object createReference(final String locationId) {
		return this;
	}

}
