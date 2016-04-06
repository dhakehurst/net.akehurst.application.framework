package net.akehurst.application.framework.realisation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class ApplicationCompositionTreeWalker {

	public ApplicationCompositionTreeWalker(ILogger logger) {
		this.logger = logger;
	}

	ILogger logger;

	class PartInfo {
		public Field field;
		public PartKind kind;
		public Class<? extends IIdentifiableObject> class_;
		public String id;
	}

	interface BuildAction {
		IIdentifiableObject execute(PartKind partKind, Class<? extends IIdentifiableObject> partClass, String partId) throws ApplicationFrameworkException;
	}

	public void build(IApplication appObject, BuildAction action) {
		this.build("", appObject, action);
	}

	private void build(String idPrefix, IIdentifiableObject object, BuildAction action) {

		List<PartInfo> partInfo = findPartInfo(idPrefix, object.getClass());

		for (PartInfo pi : partInfo) {
			try {
				IIdentifiableObject part = action.execute(pi.kind, pi.class_, pi.id);
				pi.field.set(object, part);
				this.build(idPrefix, part, action);
			} catch (Throwable t) {
				logger.log(LogLevel.ERROR, "unable to build part", t);
			}
		}

	}

	interface WalkAction {
		void execute(IIdentifiableObject tObj, String tObjId);
	}

	public void walkAndApply(IApplication applicationObject, WalkAction action) {
		action.execute(applicationObject, applicationObject.afId());
		
		List<IIdentifiableObject> parts = findParts("", applicationObject);

		for (IIdentifiableObject p : parts) {
			action.execute(p, p.afId());
			this.walkAndApply("", p, action);
		}

	}

	void walkAndApply(String idPrefix, IIdentifiableObject part, WalkAction action) {

		List<IIdentifiableObject> parts = findParts(idPrefix, part);

		for (IIdentifiableObject p : parts) {
			action.execute(p, p.afId());
			this.walkAndApply("", p, action);
		}

	}

	private List<PartInfo> findPartInfo(String idPrefix, Class<?> class_) {
		List<PartInfo> result = new ArrayList<>();
		for (Field f : class_.getDeclaredFields()) {
			PartInfo pi = findPartInfo(idPrefix, f);
			if (null == pi) {
				// ignore
			} else {
				result.add(pi);
			}
		}

		return result;
	}

	private PartInfo findPartInfo(String idPrefix, Field f) {
		f.setAccessible(true);
		PartInfo pi = null;

		ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
		if (null == ann) {
			// do nothing
		} else {
			String compId = ann.id();
			if (compId.isEmpty()) {
				compId = f.getName();
			} else {
				// do nothing
			}
			Class<? extends IComponent> fType = (Class<? extends IComponent>) f.getType();
			String partId = idPrefix + "." + compId;
			pi = new PartInfo();
			pi.class_ = fType;
			pi.field = f;
			pi.id = partId;
			pi.kind = PartKind.COMPONENT;
		}

		ActiveObjectInstance ann2 = f.getAnnotation(ActiveObjectInstance.class);
		if (null == ann2) {
			// do nothing
		} else {
			String compId = ann2.id();
			if (compId.isEmpty()) {
				compId = f.getName();
			} else {
				// do nothing
			}
			Class<? extends IComponent> fType = (Class<? extends IComponent>) f.getType();
			String partId = idPrefix + "." + compId;
			pi = new PartInfo();
			pi.class_ = fType;
			pi.field = f;
			pi.id = partId;
			pi.kind = PartKind.ACTIVE_OBJECT;
		}

		return pi;
	}

	private List<IIdentifiableObject> findParts(String idPrefix, IIdentifiableObject object) {
		List<IIdentifiableObject> result = new ArrayList<>();
		List<PartInfo> partInfo = findPartInfo(idPrefix, object.getClass());
		for (PartInfo pi : partInfo) {
			try {
				IIdentifiableObject part = (IIdentifiableObject) pi.field.get(object);
				result.add(part);
			} catch (Throwable t) {
				logger.log(LogLevel.ERROR, "unable to find part", t);
			}
		}
		return result;
	}

}
