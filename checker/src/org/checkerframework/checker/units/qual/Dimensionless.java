package org.checkerframework.checker.units.qual;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// TODO: this might not be necessary at all
/** SI base unit of seconds. */
@Documented
@Retention(RUNTIME)
@Target({TYPE_PARAMETER, TYPE_USE})
@UnitAlias("1")
public @interface Dimensionless {}
