package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.year;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar year.
 *
 * <p>This unit is used to denote a time instant in years, such as the year 2000.
 *
 * <p>The variables with this unit has its values bounded between {@literal
 * java.time.Year.MIN_VALUE} and {@literal java.time.Year.MAX_VALUE} by the Java 8 Time API. A value
 * of 0 represents 0 CE in the ISO calendar.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = year.class)
public @interface CALyear {}
