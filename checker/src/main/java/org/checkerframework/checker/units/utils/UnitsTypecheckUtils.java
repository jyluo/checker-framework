package org.checkerframework.checker.units.utils;

import javax.lang.model.element.AnnotationMirror;

/** Utility class with methods for computing the result unit of various arithmetic operations. */
public class UnitsTypecheckUtils {

    /** reference to the units representation utilities library */
    protected final UnitsRepresentationUtils unitsRepUtils;

    public UnitsTypecheckUtils(UnitsRepresentationUtils unitsRepUtils) {
        this.unitsRepUtils = unitsRepUtils;
    }

    public AnnotationMirror multiplication(AnnotationMirror lhsAM, AnnotationMirror rhsAM) {
        TypecheckUnit lhs = unitsRepUtils.createTypecheckUnit(lhsAM);
        TypecheckUnit rhs = unitsRepUtils.createTypecheckUnit(rhsAM);
        return unitsRepUtils.createInternalUnit(multiplication(lhs, rhs));
    }

    private TypecheckUnit multiplication(TypecheckUnit lhs, TypecheckUnit rhs) {
        TypecheckUnit result = new TypecheckUnit(unitsRepUtils);

        // if either lhs or rhs is UnknownUnits, then result is UnknownUnits
        if (lhs.isUnknownUnits() || rhs.isUnknownUnits()) {
            result.setUnknownUnits(true);
            return result;
        }

        // if either lhs or rhs is UnitsBottom, then result is UnitsBottom
        if (lhs.isUnitsBottom() || rhs.isUnitsBottom()) {
            result.setUnitsBottom(true);
            return result;
        }

        // otherwise res component = lhs component + rhs component
        result.setPrefixExponent(lhs.getPrefixExponent() + rhs.getPrefixExponent());
        for (String baseUnit : unitsRepUtils.baseUnits()) {
            result.setExponent(baseUnit, lhs.getExponent(baseUnit) + rhs.getExponent(baseUnit));
        }

        return result;
    }

    public AnnotationMirror division(AnnotationMirror lhsAM, AnnotationMirror rhsAM) {
        TypecheckUnit lhs = unitsRepUtils.createTypecheckUnit(lhsAM);
        TypecheckUnit rhs = unitsRepUtils.createTypecheckUnit(rhsAM);
        return unitsRepUtils.createInternalUnit(division(lhs, rhs));
    }

    private TypecheckUnit division(TypecheckUnit lhs, TypecheckUnit rhs) {
        TypecheckUnit result = new TypecheckUnit(unitsRepUtils);

        // if either lhs or rhs is UnknownUnits, then result is UnknownUnits
        if (lhs.isUnknownUnits() || rhs.isUnknownUnits()) {
            result.setUnknownUnits(true);
            return result;
        }

        // if either lhs or rhs is UnitsBottom, then result is UnitsBottom
        if (lhs.isUnitsBottom() || rhs.isUnitsBottom()) {
            result.setUnitsBottom(true);
            return result;
        }

        // otherwise res component = lhs component - rhs component
        result.setPrefixExponent(lhs.getPrefixExponent() - rhs.getPrefixExponent());
        for (String baseUnit : unitsRepUtils.baseUnits()) {
            result.setExponent(baseUnit, lhs.getExponent(baseUnit) - rhs.getExponent(baseUnit));
        }

        return result;
    }

    public boolean unitsEqual(AnnotationMirror lhsAM, AnnotationMirror rhsAM) {
        TypecheckUnit lhs = unitsRepUtils.createTypecheckUnit(lhsAM);
        TypecheckUnit rhs = unitsRepUtils.createTypecheckUnit(rhsAM);
        return lhs.equals(rhs);
    }
}
