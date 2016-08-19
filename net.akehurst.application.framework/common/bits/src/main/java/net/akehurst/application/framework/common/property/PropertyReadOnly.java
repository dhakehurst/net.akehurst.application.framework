package net.akehurst.application.framework.common.property;

public class PropertyReadOnly<T> {

	public PropertyReadOnly(final T value) {
		this.value = value;
	}

	private final T value;

	public T get() {
		return this.value;
	}

}
