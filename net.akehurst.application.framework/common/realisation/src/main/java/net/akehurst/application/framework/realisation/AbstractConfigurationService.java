package net.akehurst.application.framework.realisation;

import java.util.ArrayList;
import java.util.HashMap;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IConfiguration;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractConfigurationService extends AbstractIdentifiableObject implements IConfiguration, IService {

	@ServiceReference
	protected IApplicationFramework af;

	@ServiceReference
	protected ILogger logger;

	public AbstractConfigurationService(final String afId) {
		super(afId);
	}

	@Override
	public Object createReference(final String locationId) {
		return this;
	}

	protected <T> T createValueFromDefault(final Class<T> itemType, final String defaultValueString) {
		try {
			Object value;
			// TODO: implement default values for list and map

			if (itemType.isAssignableFrom(ArrayList.class)) {
				value = new ArrayList<>();
			} else if (itemType.isAssignableFrom(HashMap.class)) {
				value = new HashMap<>();
			} else {
				value = this.af.createDatatype(itemType, defaultValueString);
			}
			return (T) value;
		} catch (final ApplicationFrameworkException e) {
			this.logger.log(LogLevel.ERROR, e.getMessage());
			this.logger.log(LogLevel.DEBUG, e.getMessage(), e);
		}
		return null;
	}

}
