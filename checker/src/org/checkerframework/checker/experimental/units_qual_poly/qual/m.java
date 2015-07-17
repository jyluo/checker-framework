package org.checkerframework.checker.experimental.units_qual_poly.qual;

import java.lang.annotation.*;

import org.checkerframework.framework.qual.*;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;

/**
 * Meter.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TypeQualifier
@SubtypeOf(Length.class)
// This is the default:
// @UnitsRelations(org.checkerframework.checker.units.UnitsRelationsDefault.class)
// If you want an alias for "m", e.g. "Meter", simply create that
// annotation and add this meta-annotation:
// @UnitsMultiple(quantity=m.class, prefix=Prefix.one)
public @interface m {
    Prefix value() default Prefix.one;
    String param() default SimpleQualifierParameterAnnotationConverter.PRIMARY_TARGET;
}
