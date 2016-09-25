package org.checkerframework.checker.units.qual.time.duration;

import java.lang.annotation.*;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Second (1/60 of a minute).
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(TimeDuration.class)
@TimeMultiple(timeUnit = ns.class, multiplier = 1000000000L)
public @interface s {
    Prefix value() default Prefix.one;
}
