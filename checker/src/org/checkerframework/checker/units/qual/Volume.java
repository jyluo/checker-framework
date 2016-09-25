package org.checkerframework.checker.units.qual;

import java.lang.annotation.*;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Units of volume.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@SubtypeOf(UnknownUnits.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Volume {}
