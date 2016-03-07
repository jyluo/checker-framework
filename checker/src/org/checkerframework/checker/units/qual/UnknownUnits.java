package org.checkerframework.checker.units.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.type.TypeKind;

/**
 * UnknownUnits is the top type of the type hierarchy.
 *
 * UnknownUnits is the default type for any un-annotated local variables,
 * resource variables, and exception parameters, as well as for Package
 * declarations and the Void class.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@InvisibleQualifier
@SubtypeOf({})

//@DefaultQualifierInHierarchy
//@DefaultQualifierInHierarchyInUncheckedCode

// Allows flow based type refinement in the body of methods
@DefaultFor({ TypeUseLocation.LOCAL_VARIABLE
    , TypeUseLocation.IMPLICIT_UPPER_BOUND
    , TypeUseLocation.EXPLICIT_UPPER_BOUND
})
@DefaultInUncheckedCodeFor({TypeUseLocation.UPPER_BOUND})

@ImplicitFor(
        types = { TypeKind.NONE
                //, TypeKind.DECLARED
        },
        // Exceptions are always TOP type, so Throwable must also be
        typeNames = { java.lang.Throwable.class }
        )
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
public @interface UnknownUnits {}
