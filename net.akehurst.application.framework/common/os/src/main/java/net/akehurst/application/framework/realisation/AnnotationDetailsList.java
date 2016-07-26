package net.akehurst.application.framework.realisation;

import java.lang.reflect.Field;
import java.util.List;

public class AnnotationDetailsList<AET> {

	AnnotationDetailsList(Object object, Field field, Class<?> fieldType, List<AET> annotations) {
		this.object = object;
		this.field = field;
		this.annotations = annotations;
	}
	
	Object object;
	Field field;
	List<AET> annotations;
	
	public List<AET> getAnnotations() {
		return this.annotations;
	}

	public Field getField() {
		return this.field;
	}
	
	public Object getValue() {
		try {
			this.field.get(this.object);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
