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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.common.annotations.instance.IdentifiableObjectInstance;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class ApplicationCompositionTreeWalker {

	public ApplicationCompositionTreeWalker(final ILogger logger) {
		this.logger = logger;
	}

	ILogger logger;

	static public class PartInfo extends AbstractDataType {
		public PartInfo(final Field field, final PartKind kind, final Class<? extends IIdentifiableObject> class_, final String id) {
			super(field, kind, class_, id);
			this.field = field;
			this.kind = kind;
			this.class_ = class_;
			this.id = id;
		}

		Field field;
		PartKind kind;
		Class<? extends IIdentifiableObject> class_;
		String id;
	}

	interface BuildAction {
		IIdentifiableObject execute(PartKind partKind, Class<? extends IIdentifiableObject> partClass, String partId) throws ApplicationFrameworkException;
	}

	public void build(final IIdentifiableObject object, final BuildAction action) {

		final Set<PartInfo> partInfo = ApplicationCompositionTreeWalker.findPartInfo(object.afId(), object.getClass());

		for (final PartInfo pi : partInfo) {
			try {
				final IIdentifiableObject part = action.execute(pi.kind, pi.class_, pi.id);
				pi.field.set(object, part);
				// this.build(idPrefix, part, action);
			} catch (final Throwable t) {
				this.logger.log(LogLevel.ERROR, "unable to build part", t);
			}
		}

	}

	interface WalkAction {
		void execute(IIdentifiableObject tObj, String tObjId);
	}

	public void walkOneAndApply(final IIdentifiableObject obj, final WalkAction action) {
		action.execute(obj, obj.afId());

		final List<IIdentifiableObject> parts = this.findParts(obj.afId(), obj);

		for (final IIdentifiableObject p : parts) {
			action.execute(p, p.afId());
		}

	}

	public void walkAllAndApply(final IApplication applicationObject, final WalkAction action) {
		action.execute(applicationObject, applicationObject.afId());

		final List<IIdentifiableObject> parts = this.findParts(applicationObject.afId(), applicationObject);

		for (final IIdentifiableObject p : parts) {
			action.execute(p, p.afId());
			this.walkAndApply(applicationObject.afId(), p, action);
		}

	}

	void walkAndApply(final String idPrefix, final IIdentifiableObject part, final WalkAction action) {

		final List<IIdentifiableObject> parts = this.findParts(idPrefix, part);

		for (final IIdentifiableObject p : parts) {
			final String id = idPrefix + "." + p.afId();
			action.execute(p, id);
			this.walkAndApply(id, p, action);
		}

	}

	static public Set<PartInfo> findPartInfo(final String idPrefix, final Class<?> class_) {
		final Set<PartInfo> result = new HashSet<>();

		if (null == class_.getSuperclass()) {
		} else {
			final Set<PartInfo> sp = ApplicationCompositionTreeWalker.findPartInfo(idPrefix, class_.getSuperclass());
			result.addAll(sp);
		}
		for (final Field f : class_.getDeclaredFields()) {
			final PartInfo pi = ApplicationCompositionTreeWalker.findPartInfo(idPrefix, f);
			if (null == pi) {
				// ignore
			} else {
				result.add(pi);
			}
		}

		return result;
	}

	static private PartInfo findPartInfo(final String idPrefix, final Field f) {
		f.setAccessible(true);
		PartInfo pi = null;

		final ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
		if (null == ann) {
			// do nothing
		} else {
			String compId = ann.id();
			if (compId.isEmpty()) {
				compId = f.getName();
			} else {
				// do nothing
			}
			final Class<? extends IComponent> fType = (Class<? extends IComponent>) f.getType();
			final String partId = idPrefix + "." + compId;
			pi = new PartInfo(f, PartKind.COMPONENT, fType, partId);
		}

		final ActiveObjectInstance ann2 = f.getAnnotation(ActiveObjectInstance.class);
		if (null == ann2) {
			// do nothing
		} else {
			String compId = ann2.id();
			if (compId.isEmpty()) {
				compId = f.getName();
			} else {
				// do nothing
			}
			final Class<? extends IComponent> fType = (Class<? extends IComponent>) f.getType();
			final String partId = idPrefix + "." + compId;
			pi = new PartInfo(f, PartKind.ACTIVE_OBJECT, fType, partId);
		}

		final IdentifiableObjectInstance ann3 = f.getAnnotation(IdentifiableObjectInstance.class);
		if (null == ann3) {
			// do nothing
		} else {
			String objId = ann3.id();
			if (objId.isEmpty()) {
				objId = f.getName();
			} else {
				// do nothing
			}
			final Class<? extends IComponent> fType = (Class<? extends IComponent>) f.getType();
			final String partId = idPrefix + "." + objId;
			pi = new PartInfo(f, PartKind.PASSIVE_OBJECT, fType, partId);
		}

		return pi;
	}

	private List<IIdentifiableObject> findParts(final String idPrefix, final IIdentifiableObject object) {
		final List<IIdentifiableObject> result = new ArrayList<>();
		final Set<PartInfo> partInfo = ApplicationCompositionTreeWalker.findPartInfo(idPrefix, object.getClass());
		for (final PartInfo pi : partInfo) {
			try {
				final IIdentifiableObject part = (IIdentifiableObject) pi.field.get(object);
				result.add(part);
			} catch (final Throwable t) {
				this.logger.log(LogLevel.ERROR, "unable to find part", t);
			}
		}
		return result;
	}

}
