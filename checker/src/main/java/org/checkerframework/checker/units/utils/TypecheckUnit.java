package org.checkerframework.checker.units.utils;

import java.util.Map;
import java.util.Objects;
import org.checkerframework.javacutil.BugInCF;

/**
 * A data structure to encapsulate a set of java variables representing a unit for type checking.
 */
public class TypecheckUnit {
    /** reference to the units representation utilities library */
    protected final UnitsRepresentationUtils unitsRepUtils;

    private boolean uu;
    private boolean ub;
    private int prefixExponent;
    // Tree map between base units and their exponent values, maintains sorted order on base unit
    // names.
    private final Map<String, Integer> exponents;

    /**
     * Constructs a TypecheckUnit with default values, using the given {@link
     * UnitsRepresentationUtils} instance as a helper.
     *
     * @param unitsRepUtils a {@link UnitsRepresentationUtils} instance
     */
    public TypecheckUnit(UnitsRepresentationUtils unitsRepUtils) {
        this.unitsRepUtils = unitsRepUtils;

        // default UU value is false
        uu = false;
        // default UU value is false
        ub = false;
        // default prefixExponent is 0
        prefixExponent = 0;
        // default exponents are 0
        exponents = unitsRepUtils.createZeroFilledBaseUnitsMap();
    }

    /**
     * Update the boolean flag for Top to the given value.
     *
     * @param val
     */
    public void setTop(boolean val) {
        if (uu && ub) {
            throw new BugInCF("Cannot set top and bottom both to true at the same time");
        }
        uu = val;
    }

    /** @return whether this unit represents Top. */
    public boolean isTop() {
        return uu;
    }

    /**
     * Update the boolean flag for Bottom to the given value.
     *
     * @param val
     */
    public void setBottom(boolean val) {
        if (uu && ub) {
            throw new BugInCF("Cannot set top and bottom both to true at the same time");
        }
        ub = val;
    }

    /** @return whether this unit represents Bottom. */
    public boolean isBottom() {
        return ub;
    }

    /**
     * Update the prefix exponent to the given value.
     *
     * @param val
     */
    public void setPrefixExponent(int val) {
        prefixExponent = val;
    }

    /** @return the prefix exponent value. */
    public int getPrefixExponent() {
        return prefixExponent;
    }

    /**
     * Update the exponent of the given base unit to the given value.
     *
     * @param baseUnit
     * @param exp
     */
    public void setExponent(String baseUnit, int exp) {
        if (!exponents.containsKey(baseUnit)) {
            // return; // for pure performance experiment
            throw new BugInCF(
                    "Inserting exponent for base unit " + baseUnit + " which does not exist");
        }
        exponents.replace(baseUnit, exp);
    }

    /**
     * @param baseUnit
     * @return the exponent of the given base unit
     */
    public int getExponent(String baseUnit) {
        if (!exponents.containsKey(baseUnit)) {
            // return 0; // for pure performance experiment
            throw new BugInCF(
                    "Getting exponent for base unit " + baseUnit + " which does not exist");
        }
        return exponents.get(baseUnit);
    }

    /** @return the set of exponents for all base units */
    public Map<String, Integer> getExponents() {
        return exponents;
    }

    /** String representation used for debug output only. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UU = " + uu);
        sb.append(" UB = " + ub);
        sb.append(" Base-10-Prefix = " + prefixExponent);
        for (String baseUnit : unitsRepUtils.baseUnits()) {
            sb.append(" " + baseUnit + " = " + exponents.get(baseUnit));
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass().getCanonicalName(), uu, ub, prefixExponent, exponents);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TypecheckUnit other = (TypecheckUnit) obj;
        return uu == other.uu
                && ub == other.ub
                && prefixExponent == other.prefixExponent
                && exponents.equals(other.exponents);
    }
}
