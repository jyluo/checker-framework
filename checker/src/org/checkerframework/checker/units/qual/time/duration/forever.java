package org.checkerframework.checker.units.qual.time.duration;

import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Concept of forever, artificially defined in Java 8 as
 * {@linkplain Long#MAX_VALUE} seconds + 999999999 nanoseconds.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf(TimeDuration.class)
@TimeMultiple(timeUnit = s.class, multiplier = Long.MAX_VALUE + 0.999999999D)
public @interface forever {}