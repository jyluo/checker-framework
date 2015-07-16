package org.checkerframework.checker.experimental.units_qual_poly.qual;

import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@Repeatable(MultiPolyUnit.class)
public @interface PolyUnit {
    /**
     * The name of the qualifier parameter to set.
     */
    String param() default SimpleQualifierParameterAnnotationConverter.PRIMARY_TARGET;
}
