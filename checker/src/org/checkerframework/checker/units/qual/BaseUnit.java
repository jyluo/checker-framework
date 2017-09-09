package org.checkerframework.checker.units.qual;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes that a annotation is a base unit.
 *
 * <p>Base unit annotations are automatically aliased to their {@code @Unit} counterparts. For
 * example, {@code @BaseUnit public @interface m {}} declares m as a base unit, and aliases
 * {@code @m} to {@code @Unit("m")}. Any use of {@code @m} is the same as using {@code @Unit("m")}.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface BaseUnit {}
