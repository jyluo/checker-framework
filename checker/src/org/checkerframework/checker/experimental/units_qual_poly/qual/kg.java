package org.checkerframework.checker.experimental.units_qual_poly.qual;

import java.lang.annotation.*;

import org.checkerframework.framework.qual.*;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;

/**
 * Kilogram.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TypeQualifier
@SubtypeOf(Mass.class)
@UnitsMultiple(quantity=g.class, prefix=Prefix.kilo)
public @interface kg {
    String param() default SimpleQualifierParameterAnnotationConverter.PRIMARY_TARGET;
}
