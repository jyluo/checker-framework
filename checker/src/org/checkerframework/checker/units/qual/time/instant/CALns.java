package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.ns;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar nanosecond.
 *
 * <p>This unit is used to denote a time instant in nanoseconds, such as the number of nanoseconds
 * within the current millisecond.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = ns.class)
public @interface CALns {}
