package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Gregorian Year.
 *
 * <p>Java 8 defines a year as 31556952 seconds (a Gregorian year), which accounts for leap years. A
 * standard (non leap) year has exactly 365 * 24 * 60 * 60 = 31536000 seconds.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
// Gregorian leap year rule: a year is a leap year if it is evenly divisible by
// 4, but it is not a leap year if it is also evenly divisible by 100, unless it
// is also evenly divisible by 400. (eg year 2000 is a leap year, year 1900 is
// not)
@TimeMultiple(timeUnit = s.class, multiplier = 31556952L)
public @interface year {}
