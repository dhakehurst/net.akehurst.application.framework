package net.akehurst.application.framework.common.property;

public class Property<T> {

	private T value;

	public T get() {
		return this.value;
	}

	public void set(final T value) {
		this.value = value;
	}
}
