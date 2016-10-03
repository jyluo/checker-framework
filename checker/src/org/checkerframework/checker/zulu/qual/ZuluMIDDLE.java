package org.checkerframework.checker.zulu.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
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
@SubtypeOf(ZuluTOP.class)
// Option 1:
@DefaultQualifierInHierarchy

// Option 2:
//@ImplicitFor(
//    // PRIMITIVE == INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR but we don't
//    // care about CHAR (or BOOLEAN?)
//    literals = {LiteralKind.INT, LiteralKind.LONG, LiteralKind.FLOAT, LiteralKind.DOUBLE}
//)

// @DefaultInUncheckedCodeFor({TypeUseLocation.FIELD, TypeUseLocation.RETURN})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface ZuluMIDDLE {}
