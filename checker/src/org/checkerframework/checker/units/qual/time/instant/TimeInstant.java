package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.UnknownTime;
import org.checkerframework.checker.units.qual.time.duration.TimeDuration;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Dimension of time instants. A time instant is the precise time at which an event occurs, or more
 * formally a point in a time scale.
 *
 * <p>Subtypes of this type represent units of specific time instants, such as calendar years,
 * calendar day, calendar hour, etc.
 *
 * <p>Subtraction of two time instants yields a time duration, which is in a unit of time (seconds,
 * year, etc). Addition of two time instants results in error. Adding a time duration to a time
 * instant yields another time instant. Conceptually this is equivalent to "5 am + 5 hours = 10 am".
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(UnknownTime.class)
@DurationUnit(unit = TimeDuration.class)
public @interface TimeInstant {}
