/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.realisation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.akehurst.holser.reflect.BetterMethodFinder;

public class AnnotationNavigator {

    public AnnotationNavigator(final Object obj) {
        this.obj = obj;
        this.objClass = obj.getClass();
    }

    Object obj;
    Class<?> objClass;

    public <T extends Annotation> List<AnnotationDetails<T>> get(final Class<T> annotationClass) {
        return this.findPartInfo(annotationClass, this.objClass);
    }

    public <G extends Annotation, E extends Annotation> List<AnnotationDetailsList<E>> getList(final Class<G> annotationContainerClass,
            final Class<E> annotationClass) {
        return this.findPartInfo(annotationContainerClass, annotationClass, this.objClass);
    }

    <G extends Annotation, E extends Annotation> List<AnnotationDetails<E>> findPartInfo(final Class<E> annotationClass, final Class<?> class_) {
        final List<AnnotationDetails<E>> result = new ArrayList<>();

        if (null == class_.getSuperclass()) {
        } else {
            final List<AnnotationDetails<E>> sp = this.findPartInfo(annotationClass, class_.getSuperclass());
            result.addAll(sp);
        }
        for (final Field f : class_.getDeclaredFields()) {
            final AnnotationDetails<E> pi = this.findPartInfo(annotationClass, f);
            if (null == pi) {
                // ignore
            } else {
                result.add(pi);
            }
        }

        return result;
    }

    private <E extends Annotation> AnnotationDetails<E> findPartInfo(final Class<E> annotationClass, final Field f) {
        f.setAccessible(true);
        AnnotationDetails<E> pi = null;
        final E ann = f.getAnnotation(annotationClass);
        if (null == ann) {
            // do nothing
        } else {
            final Class<?> fType = f.getType();
            pi = new AnnotationDetails<>(this.obj, f, fType, ann);
        }
        return pi;
    }

    <G extends Annotation, E extends Annotation> List<AnnotationDetailsList<E>> findPartInfo(final Class<G> annotationContainerClass,
            final Class<E> annotationClass, final Class<?> class_) {
        final List<AnnotationDetailsList<E>> result = new ArrayList<>();

        if (null == class_.getSuperclass()) {
        } else {
            final List<AnnotationDetailsList<E>> sp = this.findPartInfo(annotationContainerClass, annotationClass, class_.getSuperclass());
            result.addAll(sp);
        }
        for (final Field f : class_.getDeclaredFields()) {
            final AnnotationDetailsList<E> pi = this.findPartInfo(annotationContainerClass, annotationClass, f);
            if (null == pi) {
                // ignore
            } else {
                result.add(pi);
            }
        }

        return result;
    }

    private <G extends Annotation, E extends Annotation> AnnotationDetailsList<E> findPartInfo(final Class<G> annotationContainerClass,
            final Class<E> annotationClass, final Field f) {
        f.setAccessible(true);
        AnnotationDetailsList<E> pi = null;
        final G ann = f.getAnnotation(annotationContainerClass);
        if (null == ann) {
            // do nothing
        } else {
            try {
                final Class<?> fType = f.getType();
                final BetterMethodFinder bmf = new BetterMethodFinder(annotationContainerClass);
                final Method valMethod = bmf.findMethod("value");
                final E[] values = (E[]) valMethod.invoke(ann);
                pi = new AnnotationDetailsList<>(this.obj, f, fType, Arrays.asList(values));
            } catch (final Exception e) {
                // TODO:
                e.printStackTrace();
            }
        }
        return pi;
    }
}
