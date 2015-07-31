package org.checkerframework.checker.units.qual;

import java.lang.annotation.*;

import org.checkerframework.framework.qual.*;

/**
 * A polymorphic qualifier for the Rawness type system.
 *
 * <p>
 * Any method written using @PolyUnit conceptually has
 * every instance of @PolyUnit replaced by a Units annotation.
 *
 * @checker_framework.manual #nullness-checker Nullness Checker
 */
@Documented
@TypeQualifier
@PolymorphicQualifier(UnknownUnits.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface PolyUnit { }
