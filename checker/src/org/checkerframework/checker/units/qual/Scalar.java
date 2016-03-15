package org.checkerframework.checker.units.qual;

import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchyInUncheckedCode;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Scalar is defined in Physics as a quantity that is independent of specific
 * classes of coordinate systems in other words, a quantity that has absolutely
 * no units.
 *
 * Scalar is the default type in the type hierarchy. It is the default type for
 * any un-annotated source or binary code, except for those listed in
 * {@link UnknownUnits} and {@link UnitsBottom}.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@SubtypeOf(UnknownUnits.class)
@DefaultQualifierInHierarchy
@DefaultQualifierInHierarchyInUncheckedCode
//@DefaultFor({
//    TypeUseLocation.IMPLICIT_UPPER_BOUND,       // <T>
//    // TypeUseLocation.EXPLICIT_UPPER_BOUND     // <T extends Object>
//})
@DefaultInUncheckedCodeFor({TypeUseLocation.FIELD, TypeUseLocation.RETURN})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface Scalar {}
