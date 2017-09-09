package org.checkerframework.checker.units.qual;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/** SI base unit of meters. */
@Documented
@Retention(RUNTIME)
@Target({TYPE_PARAMETER, TYPE_USE})
@BaseUnit
@SubtypeOf(Unit.class) // hack
public @interface m {}
