package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.time.duration.forever;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A conceptual time instant of forever.
 *
 * <p>In Java 8 this is artificially defined as {@linkplain Long#MAX_VALUE} seconds + 999999999
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
