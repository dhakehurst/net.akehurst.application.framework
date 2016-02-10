package net.akehurst.application.framework.common;

import java.util.Arrays;
import java.util.Objects;

abstract public class AbstractDataType {

	public AbstractDataType(Object... objects) {
		this.objects = objects;
		this.hashCode_cache = Objects.hash(this.objects);
	}

	Object[] objects;
	
	int hashCode_cache;
	@Override
	public int hashCode() {
		return this.hashCode_cache;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.getClass().isInstance(obj)) {
			AbstractDataType other = (AbstractDataType)obj;
			return Arrays.equals(this.objects, other.objects);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + Arrays.asList(this.objects);
	}

}
