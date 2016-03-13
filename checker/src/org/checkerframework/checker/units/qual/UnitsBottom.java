package org.checkerframework.checker.units.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.type.TypeKind;

/**
 * UnitsBottom is the bottom type of the type hierarchy.
 *
 * UnitsBottom is the default type for null, the void type, and the
 * {@link java.lang.Void} class. It is also the implicit and explicit lower
 * bound of a type parameter, eg {@literal <T><?>} and {@literal <? super C>}.
 *
 * It should only be used in source code to annotate the lower bounds of type
 * parameters.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
// programmatically assigned as the bottom qualifier of every units qualifier
@SubtypeOf({})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
// users can write this as the explicit lower bound of a type parameter
@TargetLocations({TypeUseLocation.EXPLICIT_LOWER_BOUND})
//it is also implicit for null literals through the ImplicitsTreeAnnotator
@ImplicitFor(
        literals = {LiteralKind.NULL},
        types = {TypeKind.NULL, TypeKind.VOID},
        typeNames = {java.lang.Void.class}
)
@DefaultFor({TypeUseLocation.IMPLICIT_LOWER_BOUND,
    //TypeUseLocation.EXPLICIT_LOWER_BOUND
    })
@DefaultInUncheckedCodeFor({TypeUseLocation.LOWER_BOUND})
public @interface UnitsBottom {}
