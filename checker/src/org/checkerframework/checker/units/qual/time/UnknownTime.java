package org.checkerframework.checker.units.qual.time;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.time.duration.TimeDuration;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * An unknown unit of time.
 *
 * <p>Subtypes of this type are split into two categories, either time instants or time durations.
 * See {@link TimeInstant} and {@link TimeDuration}.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(UnknownUnits.class)
public @interface UnknownTime {}
