package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Minute.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@TimeMultiple(timeUnit = s.class, multiplier = 60L)
public @interface min {}
