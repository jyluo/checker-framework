package org.checkerframework.checker.units.qual.time.point;

import org.checkerframework.checker.units.qual.time.duration.ns;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.*;

/**
 * Calendar nanosecond.
 *
 * This unit is used to denote a time point in nanoseconds, such as the number
 * of nanoseconds within the current millisecond.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf(TimePoint.class)
@DurationUnit(unit = ns.class)
public @interface CALns {}
