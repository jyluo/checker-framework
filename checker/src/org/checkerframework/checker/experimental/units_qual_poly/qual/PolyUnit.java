package org.checkerframework.checker.experimental.units_qual_poly.qual;

import java.lang.annotation.*;

import org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual.MultiPolyUnit;
import org.checkerframework.checker.regex.qual.MultiRegex;
import org.checkerframework.framework.qual.*;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;

/**
 * A polymorphic qualifier for the Rawness type system.
 *
 * <p>
 * Any method written using @PolyRaw conceptually has two versions:  one
 * in which every instance of @PolyRaw has been replaced by @Raw, and
 * one in which every instance of @PolyRaw has been replaced by @NonRaw.
 *
 * @checker_framework.manual #nullness-checker Nullness Checker
 */
@Documented
@TypeQualifier
@PolymorphicQualifier(UnknownUnits.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@Repeatable(MultiPolyUnit.class)
public @interface PolyUnit {
    String param() default SimpleQualifierParameterAnnotationConverter.PRIMARY_TARGET;
}

