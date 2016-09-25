package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Quarter-Year.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
// Defined as a Gregorian year / 4 = 31556952 / 4 = 7889238 seconds
@TimeMultiple(timeUnit = s.class, multiplier = 7889238L)
public @interface quarteryear {}
