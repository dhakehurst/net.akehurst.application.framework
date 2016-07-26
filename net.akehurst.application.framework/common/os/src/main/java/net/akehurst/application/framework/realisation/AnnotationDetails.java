package net.akehurst.application.framework.realisation;

import java.lang.reflect.Field;

public class AnnotationDetails<AT> {

	AnnotationDetails(Object object, Field field, Class<?> fieldType, AT annotation) {
		this.object = object;
		this.field = field;
		this.annotation = annotation;
	}
	
	Object object;
	Field field;
	AT annotation;
	
	public AT getAnnotation() {
		return this.annotation;
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
