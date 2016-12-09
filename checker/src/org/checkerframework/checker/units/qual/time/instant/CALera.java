package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.era;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar era.
 *
 * <p>This unit is used to denote a time instant in eras, such as BCE and CE.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = era.class)
public @interface CALera {}
