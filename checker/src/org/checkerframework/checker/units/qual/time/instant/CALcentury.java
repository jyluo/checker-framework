package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.century;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar century.
 *
 * <p>This unit is used to denote a time instant in centuries, such as 21st century.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = century.class)
public @interface CALcentury {}
