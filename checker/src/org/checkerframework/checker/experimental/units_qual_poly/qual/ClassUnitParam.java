package org.checkerframework.checker.experimental.units_qual_poly.qual;

import org.checkerframework.checker.tainting.qual.ClassTaintingParam;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * ClassRegexParam declares a regex qualifier parameter on a class.
 *
 * @see ClassTaintingParam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(MultiClassUnitParam.class)
public @interface ClassUnitParam {
    /**
     * The name of the qualifier parameter to declare.
     */
    String value() default SimpleQualifierParameterAnnotationConverter.PRIMARY_TARGET;
}
