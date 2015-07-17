package org.checkerframework.checker.experimental.units_qual_poly.qual;

import java.lang.annotation.*;

/**
 * Specify the class that knows how to handle the meta-annotated unit
 * when put in relation (plus, multiply, ...) with another unit.
 *
 * @see org.checkerframework.checker.experimental.units_qual_poly.UnitsQualifiedRelations
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitsRelations {
    /**
     * @return The UnitsRelations subclass to use.
     */
    Class<? extends org.checkerframework.checker.experimental.units_qual_poly.UnitsQualifiedRelations> value();
}
