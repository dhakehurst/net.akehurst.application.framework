package net.akehurst.application.framework.common.test;

import net.akehurst.application.framework.common.IConfiguration;
import net.akehurst.application.framework.realisation.AbstractIdentifiableObject;

public class AbstractMockConfigurationService extends AbstractIdentifiableObject implements IConfiguration {

	public AbstractMockConfigurationService(final String afId) {
		super(afId);
	}

	@Override
	public <T> T fetchValue(final Class<T> itemType, final String idPath, final String defaultValueString) {
		// TODO Auto-generated method stub
		return null;
	}

}
