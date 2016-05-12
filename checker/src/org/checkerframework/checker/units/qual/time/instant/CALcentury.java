package org.checkerframework.checker.units.qual.time.instant;

import org.checkerframework.checker.units.qual.time.duration.century;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.*;

/**
 * Calendar century.
 *
 * This unit is used to denote a time instant in centuries, such as 21st century.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = century.class)
public @interface CALcentury {}
