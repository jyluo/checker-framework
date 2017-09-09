package org.checkerframework.checker.units.qual;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.checkerframework.framework.qual.TypeUseLocation.UPPER_BOUND;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.SubtypeOf;

@Documented
@Retention(RUNTIME)
@Target({TYPE_PARAMETER, TYPE_USE})
@SubtypeOf({})
@DefaultFor(UPPER_BOUND)
public @interface UnknownUnits {}
