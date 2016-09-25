package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.UnknownTime;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Units of time duration. A time duration represents the duration of a single
 * event or the interval between two events, or more formally a length in a time
 * scale between two time instants.
 *
 * Subtypes of this type represent units of specific time durations, such as
 * hours, mins, seconds, etc.
 *
 * Also see {@link TimeInstant}
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@SubtypeOf(UnknownTime.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface TimeDuration {}
