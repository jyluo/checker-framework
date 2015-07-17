package org.checkerframework.checker.experimental.units_qual_poly.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;

/**
 * MixedUnits is the result of multiplying or dividing units, where no more
 * specific unit is known from a UnitsRelations implementation.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@TypeQualifier
@SubtypeOf(UnknownUnits.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface MixedUnits {}
