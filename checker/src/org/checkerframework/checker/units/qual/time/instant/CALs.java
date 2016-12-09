package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.s;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar second.
 *
 * <p>This unit is used to denote a time instant in seconds, such as the seconds within the current
 * minute.
 *
 * <p>The variables with this unit has its values bounded between 1 to 60 by the Java 8 Time API.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = s.class)
public @interface CALs {}
