package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.month;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar month.
 *
 * This unit is used to denote a time instant in months, such as the month
 * within the current year.
 *
 * The variables with this unit has its values bounded between 1 to 12 by the
 * Java 8 Time API.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = month.class)
public @interface CALmonth {}
