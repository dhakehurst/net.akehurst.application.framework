package net.akehurst.application.framework.common.property;

import java.util.List;

public class PropertyList<T> {

	private List<T> value;

	public List<T> get() {
		return this.value;
	}

	public void set(final List<T> value) {
		this.value = value;
	}

	public void add(final T value) {
		this.value.add(value);
	}
}
