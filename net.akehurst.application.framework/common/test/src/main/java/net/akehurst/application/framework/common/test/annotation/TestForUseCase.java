package net.akehurst.application.framework.common.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface TestForUseCase {
    String usecaseId() default "";
}
