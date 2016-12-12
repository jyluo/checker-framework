package org.checkerframework.checker.units.qual;

import java.lang.annotation.Annotation;

/** Defines the relation between a base unit and the current metric-prefixed unit. */
public @interface UnitsMultiple {
    /** @return the base unit to use */
    Class<? extends Annotation> quantity();

    /** @return the scaling prefix */
    Prefix prefix() default Prefix.one;
}
