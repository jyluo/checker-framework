package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Week.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
// 86400 * 7 = 604800
@TimeMultiple(timeUnit = s.class, multiplier = 604800L)
public @interface week {}
