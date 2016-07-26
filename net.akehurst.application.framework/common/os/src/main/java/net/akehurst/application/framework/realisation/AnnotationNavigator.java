package net.akehurst.application.framework.realisation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.akehurst.holser.reflect.BetterMethodFinder;

public class AnnotationNavigator {

	public AnnotationNavigator(Object obj) {
		this.obj = obj;
		this.objClass = obj.getClass();
	}
	Object obj;
	Class<?> objClass;
	
	public <T extends Annotation> List<AnnotationDetails<T>> get(Class<T> annotationClass) {
		return this.findPartInfo(annotationClass, this.objClass);
	}
	
	public <G extends Annotation,E extends Annotation> List<AnnotationDetailsList<E>> getList(Class<G> annotationContainerClass, Class<E> annotationClass) {
		return this.findPartInfo(annotationContainerClass, annotationClass, this.objClass);
	}
	
	<G extends Annotation,E extends Annotation> List<AnnotationDetails<E>> findPartInfo(Class<E> annotationClass,  Class<?> class_) {
		List<AnnotationDetails<E>> result = new ArrayList<>();

		if (null == class_.getSuperclass()) {
		} else {
			List<AnnotationDetails<E>> sp = findPartInfo(annotationClass,  class_.getSuperclass());
			result.addAll( sp );
		}
		for (Field f : class_.getDeclaredFields()) {
			AnnotationDetails<E> pi = findPartInfo(annotationClass,  f);
			if (null == pi) {
				// ignore
			} else {
				result.add(pi);
			}
		}

		return result;
	}
	
	private <E extends Annotation> AnnotationDetails<E> findPartInfo(Class<E> annotationClass, Field f) {
		f.setAccessible(true);
		AnnotationDetails<E> pi = null;
		E ann = f.getAnnotation(annotationClass);
		if (null == ann) {
			// do nothing
		} else {
			Class<?> fType = (Class<?>) f.getType();
			pi = new AnnotationDetails<E>(this.obj, f, fType, ann);
		}
		return pi;
	}

	<G extends Annotation,E extends Annotation> List<AnnotationDetailsList<E>> findPartInfo(Class<G> annotationContainerClass, Class<E> annotationClass,  Class<?> class_) {
		List<AnnotationDetailsList<E>> result = new ArrayList<>();

		if (null == class_.getSuperclass()) {
		} else {
			List<AnnotationDetailsList<E>> sp = findPartInfo(annotationContainerClass, annotationClass,  class_.getSuperclass());
			result.addAll( sp );
		}
		for (Field f : class_.getDeclaredFields()) {
			AnnotationDetailsList<E> pi = findPartInfo(annotationContainerClass, annotationClass,  f);
			if (null == pi) {
				// ignore
			} else {
				result.add(pi);
			}
		}

		return result;
	}
	
	private <G extends Annotation,E extends Annotation> AnnotationDetailsList<E> findPartInfo(Class<G> annotationContainerClass, Class<E> annotationClass, Field f) {
		f.setAccessible(true);
		AnnotationDetailsList<E> pi = null;
		G ann = f.getAnnotation(annotationContainerClass);
		if (null == ann) {
			// do nothing
		} else {
			try {
			Class<?> fType = (Class<?>) f.getType();
			BetterMethodFinder bmf = new BetterMethodFinder(annotationContainerClass);
			Method valMethod = bmf.findMethod("value");
			E[] values = (E[])valMethod.invoke(ann);
			pi = new AnnotationDetailsList<E>(this.obj, f, fType, Arrays.asList(values));
			} catch(Exception e) {
				//TODO:
				e.printStackTrace();
			}
		}
		return pi;
	}
}
