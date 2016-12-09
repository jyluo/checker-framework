package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.us;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar microsecond.
 *
 * <p>This unit is used to denote a time instant in microseconds.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = us.class)
public @interface CALus {}
