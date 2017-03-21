package net.akehurst.application.framework.common.test;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.realisation.AbstractConfigurationService;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class TestConfigurationService extends AbstractConfigurationService {

	public TestConfigurationService(final String afId) {
		super(afId);
		this.testValues = new HashMap<>();
	}

	Map<String, Object> testValues;

	public void set(final String idPath, final Object value) {
		this.testValues.put(idPath, value);
	}

	@Override
	public <T> T fetchValue(final Class<T> itemType, final String idPath, final String defaultValueString) {
		T value = (T) this.testValues.get(idPath);
		if (null == value) {
			value = super.createValueFromDefault(itemType, defaultValueString);
		}
		this.logger.log(LogLevel.TRACE,
				String.format("%s.fetchValue(%s,%s) = %s", this.afId(), itemType.getName(), idPath, null == value ? "null" : value.toString()));
		return value;
	}

}
