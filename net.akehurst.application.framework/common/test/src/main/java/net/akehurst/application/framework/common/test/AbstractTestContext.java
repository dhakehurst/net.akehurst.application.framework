package net.akehurst.application.framework.common.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.realisation.AbstractActiveObject;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.test.ITestEnvironment;

abstract public class AbstractTestContext extends AbstractActiveObject implements ITestEnvironment {

	public AbstractTestContext() {
		super("testEnvironment");
	}

	@Override
	public void afRun() {
		try {
			// TODO handle inheritance ?
			final List<IActiveObject> objects = new ArrayList<>();
			for (final Field f : this.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				final ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
				if (null == ann) {
					// do nothing
				} else {
					final IActiveObject ao = (IActiveObject) f.get(this);
					// TODO: support ordering of objects
					objects.add(ao);
				}

				final ActiveObjectInstance ann2 = f.getAnnotation(ActiveObjectInstance.class);
				if (null == ann2) {
					// do nothing
				} else {
					final IActiveObject ao = (IActiveObject) f.get(this);
					// TODO: support ordering of objects
					objects.add(ao);
				}
			}

			for (final IActiveObject ao : objects) {
				ao.afStart();
			}

			for (final IActiveObject ao : objects) {
				ao.afJoin();
			}

		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Failed to run application " + this.afId(), ex);
		}
	}

}
