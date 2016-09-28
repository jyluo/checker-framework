package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.time.duration.forever;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A conceptual time instant of forever in the past or future, artificially
 * defined in Java 8 as {@linkplain Long#MAX_VALUE} seconds + 999999999
 * nanoseconds.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeInstant.class)
@DurationUnit(unit = forever.class)
public @interface CALforever {}