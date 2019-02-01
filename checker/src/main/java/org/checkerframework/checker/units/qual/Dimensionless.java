package org.checkerframework.checker.units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A dimensionless "unit".
 *
 * @checker_framework.manual #units-checker Units Checker
 */
// TODO: replace @UnitsAlias with @UnitsRep
@UnitsAlias(baseUnitComponents = {})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Dimensionless {}
