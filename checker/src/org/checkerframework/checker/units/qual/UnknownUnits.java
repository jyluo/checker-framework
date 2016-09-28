package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchyInUncheckedCode;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * UnknownUnits is the top type of the type hierarchy.
 *
 * UnknownUnits is the default type for any un-annotated local variables,
 * resource variables, exceptions and exception parameters, and for the {@link java.lang.Throwable} class.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@InvisibleQualifier
@SubtypeOf({})
@DefaultQualifierInHierarchyInUncheckedCode

// Option 1:
//@DefaultFor({
//    // Allows flow based type refinement in the body of methods
//    TypeUseLocation.LOCAL_VARIABLE,
//    TypeUseLocation.EXCEPTION_PARAMETER,
//    TypeUseLocation.RESOURCE_VARIABLE,
//    TypeUseLocation.IMPLICIT_UPPER_BOUND, // <T>
//    TypeUseLocation.EXPLICIT_UPPER_BOUND // <T extends Object>
//})
// @DefaultInUncheckedCodeFor({TypeUseLocation.UPPER_BOUND})
//Exceptions are always TOP type, so Throwable must be as well
//@ImplicitFor(typeNames = {java.lang.Throwable.class})

// Option 2:
@DefaultQualifierInHierarchy
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface UnknownUnits {}
