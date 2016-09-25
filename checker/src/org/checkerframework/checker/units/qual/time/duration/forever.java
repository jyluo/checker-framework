package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A conceptual duration of forever, artificially defined in Java 8 as
 * {@linkplain Long#MAX_VALUE} seconds + 999999999 nanoseconds.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@TimeMultiple(timeUnit = s.class, multiplier = Long.MAX_VALUE + 0.999999999D)
public @interface forever {}
