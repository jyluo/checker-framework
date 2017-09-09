package org.checkerframework.checker.units.qual;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes a unit as an alias unit.
 *
 * <p>TODO: usage documentation.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface UnitAlias {
    String value() default "1";
}
