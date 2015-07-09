package org.checkerframework.checker.experimental.units_qual;

import org.checkerframework.qualframework.base.Checker;

/**
 * {@link Checker} for the Units-Qual type system.
 */
public class UnitsQualChecker extends Checker<Units>{
    @Override
    protected UnitsQualifiedTypeFactory createTypeFactory(){
        return new UnitsQualifiedTypeFactory(this);
    }
}
