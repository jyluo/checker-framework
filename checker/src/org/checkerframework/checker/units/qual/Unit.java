package org.checkerframework.checker.units.qual;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Unit denotes a scientific unit. The specific unit is specified in the single string parameter of
 * the annotation.
 *
 * <p>The allowed units in the string is any combination of base units and the special unit of "1"
 * which denotes a dimensionless quantity.
 *
 * <p>For example, {@code @Unit("1/s")} may be used to indicate the unit of Hertz.
 *
 * <p>The default unit for all program elements is {@code @Unit("1")}.
 *
 * <p>TODO: string syntax documentation.
 *
 * <p>TODO: normalization documentation.
 *
 * <p>TODO: base unit definition and usage documentation.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE_USE, TYPE_PARAMETER})
@SubtypeOf(UnknownUnits.class)
@DefaultQualifierInHierarchy
public @interface Unit {
    String value() default "1";
}
