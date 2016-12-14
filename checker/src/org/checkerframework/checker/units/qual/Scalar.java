package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A Scalar is defined in Physics as a quantity that is independent of specific classes of
 * coordinate systems in other words, a quantity that has absolutely no units.
 *
 * <p>Scalar is the default type in the type hierarchy. It is the default type for any un-annotated
 * source or binary code, except for those listed in {@link UnknownUnits} and {@link UnitsBottom}.
 *
 * <p>It is also the default type for the implicit and explicit upper bound of a type parameter, eg
 * {@literal <T>} and {@literal <T extends C>}.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@DefaultQualifierInHierarchy
// @DefaultInUncheckedCodeFor({TypeUseLocation.FIELD, TypeUseLocation.RETURN})
// @ImplicitFor(literals = {LiteralKind.INT, LiteralKind.LONG, LiteralKind.FLOAT, LiteralKind.DOUBLE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(UnknownUnits.class)
public @interface Scalar {}
