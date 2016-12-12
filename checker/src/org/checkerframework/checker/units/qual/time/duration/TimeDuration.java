package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.UnknownTime;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Dimension of time duration. A time duration represents the duration of a single event or the
 * interval between two events, or more formally a length in a time scale between two time instants.
 *
 * <p>Subtypes of this type represent units of specific time durations, such as hours, mins,
 * seconds, etc.
 *
 * <p>Also see {@link TimeInstant}
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(UnknownTime.class)
public @interface TimeDuration {}
