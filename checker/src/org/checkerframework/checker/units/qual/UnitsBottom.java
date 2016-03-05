package org.checkerframework.checker.units.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultInUncheckedCodeFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;
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
 * @checker_framework.manual #units-checker Units Checker
 */
@SubtypeOf({}) // programmatically assigned as the bottom qualifier of every units qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@DefaultFor({TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.IMPLICIT_LOWER_BOUND})
@DefaultInUncheckedCodeFor({TypeUseLocation.LOWER_BOUND})
//it is also implicit for null literals through the ImplicitsTreeAnnotator
@ImplicitFor(types = {TypeKind.VOID, TypeKind.NULL}, typeNames = {java.lang.Void.class}
    , literals = {LiteralKind.NULL}
)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface UnitsBottom {}
