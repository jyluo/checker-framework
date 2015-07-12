package org.checkerframework.checker.experimental.units_qual_poly;


import org.checkerframework.checker.experimental.units_qual_poly.Units;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
import org.checkerframework.qualframework.base.CheckerAdapter;
import org.checkerframework.qualframework.poly.PolyQual.GroundQual;
import org.checkerframework.qualframework.poly.QualParams;

/**
 * {@link CheckerAdapter} for the Units-Qual-Param type system.
 */
public class UnitsChecker extends CheckerAdapter<QualParams<Units>> {

    public UnitsChecker() {
        super(new UnitsQualPolyChecker());
    }

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new UnitsTypecheckVisitor(this);
    }

    @Override
    public void setupDefaults(QualifierDefaults defaults) {
        
        // default qualifiers for different kinds of code elements
        
        // TODO: verify correctness
        
        defaults.addAbsoluteDefault(
                getTypeMirrorConverter().getAnnotation(
                        new QualParams<>(new GroundQual<>(Units.BOTTOM))),
                DefaultLocation.LOWER_BOUNDS);

        defaults.addAbsoluteDefault(
                getTypeMirrorConverter().getAnnotation(
                        new QualParams<>(new GroundQual<>(Units.UnitsUnknown))),
                DefaultLocation.LOCAL_VARIABLE);
    }

}
