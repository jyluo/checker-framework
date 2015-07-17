package org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual;

import org.checkerframework.checker.experimental.units_qual_poly.qual.PolyUnit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface MultiPolyUnit {
    PolyUnit[] value();
}
