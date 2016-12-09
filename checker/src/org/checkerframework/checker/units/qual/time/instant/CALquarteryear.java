package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.quarteryear;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar quarter-year.
 *
 * <p>This unit is used to denote a time instant in quarter-year, such as the 4 different seasons of
 * the year.
 *
 * <p>The variables with this unit has its values bounded between 1 to 4 by the Java 8 Time API.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = quarteryear.class)
public @interface CALquarteryear {}
