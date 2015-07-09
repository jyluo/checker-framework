package org.checkerframework.checker.experimental.units_qual;

import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.qualframework.base.CheckerAdapter;

/**
 * {@link CheckerAdapter} for the Units-Qual type system.
 */
public class UnitsCheckerAdapter extends CheckerAdapter<Units>{

    public UnitsCheckerAdapter(){
        super(new UnitsQualChecker());
    }

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new UnitsTypecheckVisitor(this);
    }
}
