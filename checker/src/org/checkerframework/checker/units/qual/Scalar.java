package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A Scalar is defined in Physics as a quantity that is independent of specific
 * classes of coordinate systems in other words, a quantity that has absolutely
 * no units.
 *
 * Scalar is the default type in the type hierarchy. It is the default type for
 * any un-annotated source or binary code, except for those listed in
 * {@link UnknownUnits} and {@link UnitsBottom}.
 *
 * It is also the default type for the implicit and explicit upper bound of a
 * type parameter, eg {@literal <T>} and {@literal <T extends C>}.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@SubtypeOf(UnknownUnits.class)
// Option 1:
// @DefaultQualifierInHierarchy

// Op 1 optional:
// @DefaultFor({
// TypeUseLocation.IMPLICIT_UPPER_BOUND, // <T>
// TypeUseLocation.EXPLICIT_UPPER_BOUND // <T extends Object>
// })

// Option 2:
@ImplicitFor(
    // PRIMITIVE == INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR but we don't
    // care about CHAR (or BOOLEAN?)
    literals = {
        LiteralKind.INT,
        LiteralKind.LONG,
        LiteralKind.FLOAT,
        LiteralKind.DOUBLE,
        LiteralKind.BOOLEAN
    }
)

// @DefaultInUncheckedCodeFor({TypeUseLocation.FIELD, TypeUseLocation.RETURN})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Scalar {}
