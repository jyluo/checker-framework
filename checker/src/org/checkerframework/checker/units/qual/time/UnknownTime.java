package org.checkerframework.checker.units.qual.time;

import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.time.duration.TimeDuration;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.*;

/**
 * An unknown unit of time. Subtypes of this type are split into two
 * categories, either time instants or time durations. See {@link TimeInstant}
 * and {@link TimeDuration}.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@SubtypeOf(UnknownUnits.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface UnknownTime {}
