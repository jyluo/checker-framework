package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.duration.decade;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Calendar decade.
 *
 * <p>This unit is used to denote a time instant in decades, such as the 20s and 50s.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = decade.class)
public @interface CALdecade {}
