package org.checkerframework.checker.experimental.units_qual_poly.qual;

import java.lang.annotation.*;

import org.checkerframework.framework.qual.*;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;

/**
 * Ampere.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TypeQualifier
@SubtypeOf(Current.class)
public @interface A {
    Prefix value() default Prefix.one;
    String param() default SimpleQualifierParameterAnnotationConverter.PRIMARY_TARGET;
}
