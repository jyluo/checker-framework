package org.checkerframework.checker.units.qual.time.instant;

import org.checkerframework.checker.units.qual.time.duration.millennia;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.*;

/**
 * Calendar millennium.
 *
 * This unit is used to denote a time instant in millennia, where the first
 * millennia spans 1 CE to 1000 CE.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = millennia.class)
public @interface CALmillennia {}
