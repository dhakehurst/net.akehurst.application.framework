package net.akehurst.application.framework.common.annotations.declaration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProvidesInterfaceForPorts {
	ProvidesInterfaceForPort[] value();
}
