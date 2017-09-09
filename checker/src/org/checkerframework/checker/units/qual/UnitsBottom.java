package org.checkerframework.checker.units.qual;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.checkerframework.framework.qual.LiteralKind.NULL;
import static org.checkerframework.framework.qual.TypeUseLocation.EXPLICIT_LOWER_BOUND;
import static org.checkerframework.framework.qual.TypeUseLocation.EXPLICIT_UPPER_BOUND;
import static org.checkerframework.framework.qual.TypeUseLocation.LOWER_BOUND;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;

/**
 * The bottom type in the Units type system. Programmers should rarely write this type.
 *
 * @checker_framework.manual #units-checker Units Checker
 * @checker_framework.manual #bottom-type the bottom type
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE_PARAMETER, TYPE_USE})
@SubtypeOf(Unit.class)
@DefaultFor(LOWER_BOUND)
@ImplicitFor(typeNames = Void.class, literals = NULL)
@TargetLocations({EXPLICIT_LOWER_BOUND, EXPLICIT_UPPER_BOUND})
public @interface UnitsBottom {}
