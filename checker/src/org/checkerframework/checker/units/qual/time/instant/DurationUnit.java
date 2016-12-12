package org.checkerframework.checker.units.qual.time.instant;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the relation between a time instant unit and it's corresponding time duration unit.
 *
 * <p>This meta-annotation is mandatory on all time instant units, and must map to a corresponding
 * time duration unit.
 *
 * <p>E.g. two calendar years (time instant) are separated by a number of years (time duration).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DurationUnit {
    /** @return The base time unit. */
    Class<? extends Annotation> unit();
}
