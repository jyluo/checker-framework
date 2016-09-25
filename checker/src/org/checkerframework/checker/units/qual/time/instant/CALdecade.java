package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.decade;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar decade.
 *
 * This unit is used to denote a time instant in decades, such as the 20s and
 * 50s.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = decade.class)
public @interface CALdecade {}
