package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the relation between a time instant unit and it's corresponding time duration unit.
 *
 * E.g. two calendar years (time instant) are separated by a year (time duration).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DurationUnit {
    /**
     * @return The base time unit.
     */
    Class<? extends Annotation> unit();
}
