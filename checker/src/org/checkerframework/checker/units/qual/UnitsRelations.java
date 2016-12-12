package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Meta-annotation used to designate each class that defines relations between units of arithmetic
 * operations.
 *
 * @see org.checkerframework.checker.units.UnitsRelations
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitsRelations {
    /** @return the org.checkerframework.checker.units.UnitsRelations subclass to use */
    // The more precise type is Class<? extends org.checkerframework.checker.units.UnitsRelations>,
    // but org.checkerframework.checker.units.UnitsRelations is not in checker-qual.jar, nor can
    // it be since it uses AnnotatedTypeMirrors. So use a less precise type and check that it is a
    // sub class in UnitsAnnotatedTypeFactory
    Class<?> value();
}
