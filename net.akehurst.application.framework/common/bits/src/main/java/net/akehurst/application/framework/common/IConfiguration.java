package net.akehurst.application.framework.common;

public interface IConfiguration {

	<T> T fetchValue(Class<T> itemType, String idPath, String defaultValueString);
}
