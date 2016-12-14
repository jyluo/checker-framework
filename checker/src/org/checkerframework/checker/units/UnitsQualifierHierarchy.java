package org.checkerframework.checker.units;

import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

public final class UnitsQualifierHierarchy extends GraphQualifierHierarchy {

    UnitsAnnotatedTypeFactory factory;

    public UnitsQualifierHierarchy(
            MultiGraphFactory mgf, AnnotationMirror bottom, UnitsAnnotatedTypeFactory uFactory) {
        // Programatically set the Bottom qualifier as the bottom of the hierarchy.
        super(mgf, bottom);
        factory = uFactory;
    }

    @Override
    public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
        if (AnnotationUtils.areSameIgnoringValues(lhs, rhs)) {
            return AnnotationUtils.areSame(lhs, rhs);
        }
        lhs = factory.removePrefix(lhs);
        rhs = factory.removePrefix(rhs);

        return super.isSubtype(rhs, lhs);
    }

    // Overriding leastUpperBound due to the fact that alias annotations are not placed in the
    // Supported Type Qualifiers set, instead, their base SI units are in the set.
    // Whenever an alias annotation or prefix-multiple of a base SI unit is used, we handle the
    // LUB resolution here.
    @Override
    public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {
        AnnotationMirror result;

        // if the prefix is Prefix.one, automatically strip it for LUB
        // checking
        if (UnitsRelationsTools.getPrefix(a1) == Prefix.one) {
            a1 = factory.removePrefix(a1);
        }
        if (UnitsRelationsTools.getPrefix(a2) == Prefix.one) {
            a2 = factory.removePrefix(a2);
        }

        // if the two units have the same base SI unit
        if (UnitsRelationsTools.areSameUnitsIgnoringPrefix(a1, a2)) {
            if (UnitsRelationsTools.areSameUnits(a1, a2)) {
                // and if they have the same Prefix, it means it is the same
                // unit, so we return the unit
                result = a1;
            } else {
                // if they don't have the same Prefix, find the LUB

                // check if a1 is a prefixed multiple of a base unit
                boolean a1Prefixed = !UnitsRelationsTools.hasNoPrefix(a1);
                // check if a2 is a prefixed multiple of a base unit
                boolean a2Prefixed = !UnitsRelationsTools.hasNoPrefix(a2);

                // when calling findLub(), the left AnnoMirror has to be a
                // type within the supertypes Map
                // this means it has to be one of the base SI units, so
                // always strip the left unit or ensure it has no prefix
                if (a1Prefixed && a2Prefixed) {
                    // if both are prefixed, strip the left and find LUB
                    result = this.findLub(factory.removePrefix(a1), a2);
                } else if (a1Prefixed && !a2Prefixed) {
                    // if only the left is prefixed, swap order and find LUB
                    result = this.findLub(a2, a1);
                } else {
                    // else (only right is prefixed), just find the LUB
                    result = this.findLub(a1, a2);
                }
            }
        } else {
            // if they don't have the same base SI unit, let super find it
            result = super.leastUpperBound(a1, a2);
        }

        return result;
    }
}
