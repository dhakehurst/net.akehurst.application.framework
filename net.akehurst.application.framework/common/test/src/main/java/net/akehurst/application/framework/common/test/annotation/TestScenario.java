package net.akehurst.application.framework.common.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface TestScenario {
	String description();
}
