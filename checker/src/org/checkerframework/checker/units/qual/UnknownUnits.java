package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchyInUncheckedCode;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * UnknownUnits is the top type of the type hierarchy.
 *
 * <p>UnknownUnits is the default type for any un-annotated local variables, resource variables,
 * exceptions and exception parameters, and for the {@link java.lang.Throwable} class.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@DefaultQualifierInHierarchyInUncheckedCode

// Option 1:
@DefaultInUncheckedCodeFor({TypeUseLocation.UPPER_BOUND})
//Exceptions are always TOP type, so Throwable must be as well
@ImplicitFor(typeNames = {java.lang.Throwable.class})
@DefaultFor({
    // Allows flow based type refinement in the body of methods
    TypeUseLocation.LOCAL_VARIABLE, // for flow based refinement
    TypeUseLocation.EXCEPTION_PARAMETER, // exceptions are always top
    TypeUseLocation.RESOURCE_VARIABLE, // to allow foreach loops to loop over collections
    TypeUseLocation.IMPLICIT_UPPER_BOUND, // <T>, so that T can take on any type in usage
    // TypeUseLocation.EXPLICIT_UPPER_BOUND, // <T extends Object>
    TypeUseLocation
            .RECEIVER // classes are Scalar by default, but we can have objects where the unit is UnknownUnits.
})

/*
 * For receiver
 *
 * we need this because classes are all by default Scalar, but we can have local references to objects where the object is UnknownUnits.
 *
 * tests/all-systems/TypeVarAndArrayRefinement.java:14: error: [method.invocation.invalid] call to name() not allowed on the given receiver.
 *            if (constant.name().equalsIgnoreCase(name.replace('-', '_'))) {
 *                             ^
 *  found   : @UnknownUnits Enum
 *  required: @Scalar Enum
 * 1 error
 *
 */

// the rest:
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({})
public @interface UnknownUnits {}
