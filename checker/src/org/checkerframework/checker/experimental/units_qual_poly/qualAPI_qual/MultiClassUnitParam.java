package org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MultiClassUnitParam {
    ClassUnitParam[] value();
}

