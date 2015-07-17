package org.checkerframework.checker.experimental.units_qual_poly;

import java.lang.annotation.Annotation;
//import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.experimental.units_qual_poly.qual.*;

// TODO : add some kind of way to ensure that there are no two units with the same name and prefix, perhaps by adapting this into a factory-pool

// TODO 2 : subclass units like mPREs2 into plural Units so that users can define their own custom mixed units that are based upon a mathematical definition (eg x/y, x^n)

public class Units {
    //In the Units system, each unit can have at most one super type.
    protected final String unitName;    // name of the Unit
    protected final Prefix prefix;      // prefix of the Unit
    protected final Units superType;    // super Type of the Unit in the qualifier hierarchy
    protected final Class<? extends Annotation> annotation;     // optional: a reference to the classic qualifier annotation

    // Four constructors for creating a Units qualifier based on existing annotations

    // constructor for units which have no defined super type, and no defined prefix
    // by default they have UnitsUnknown as their super type, and prefix of Prefix.one
    protected Units(String name, Class<? extends Annotation> anno) {
        unitName = name;
        prefix = Prefix.one;
        superType = Units.UNITSUNKNOWN;
        annotation = anno;
    }

    // constructor for units with no defined super type, by default they have UnitsUnknown as their super type
    protected Units(String name, Prefix p, Class<? extends Annotation> anno) {
        unitName = name;
        prefix = p;
        superType = Units.UNITSUNKNOWN;
        annotation = anno;
    }

    // constructor for units with no defined prefix, by default it will have a prefix of Prefix.one
    protected Units(String name, Units superUnit, Class<? extends Annotation> anno) {
        unitName = name;
        prefix = Prefix.one;
        superType = superUnit;
        annotation = anno;
    }

    // constructor for units with a defined super type and prefix
    protected Units(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
        unitName = name;
        prefix = p;
        superType = superUnit;
        annotation = anno;
    }

    @Override
    public String toString() {
        return "Units(" + unitName + ", prefix = " + prefix +")";
    }

    public String getUnitName() {
        return unitName;
    }

    public Prefix getPrefix() {
        return prefix;
    }

    public Units getSuperType() {
        return superType;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    /**
     * checks to see if the current unit is a super type of the unit passed in as parameter
     * @param unit
     * @return  true if it is, false if it isn't
     */
    public boolean isSuperType(Units unit) {

        // bottom is never a super type of any other type
        if(this.equals(BOTTOM))
            return false;
        // everything else is always a super type of bottom
        if(unit.equals(BOTTOM))
            return true;
        // else see if the unit's super type is "this"
        if(unit.superType.equals(this)) {
            return true;
        }
        return false;
    }

    /**
     * checks to see if the current unit is a sub type of the unit passed in as parameter
     * @param unit
     * @return true if it is, false if it isn't
     */
    public boolean isSubType(Units unit) {
        //System.out.println((this == null) + " " + (unit == null));
        //System.out.println(this.toString() + " " + unit.toString());

        // units unknown is never a sub type of anything else
        if(this.equals(UNITSUNKNOWN))
            return false;
        // everything else is always a sub type of UnitsUnknown
        if(unit.equals(UNITSUNKNOWN))
            return true;
        // else see if "this" unit's super type matches unit
        if(this.superType.equals(unit)) {
            return true;
        }
        return false;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        result = prime * result
                + ((unitName == null) ? 0 : unitName.hashCode());
        return result;
    }

    /**
     * Compares one unit to another based on their names and prefixes
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;

        Units other = (Units) obj;

        if( this.unitName == null || other.unitName == null ) return false;

        // two units are equal if their names and prefixes are the same
        if(! (this.unitName.equals(other.unitName) && (this.prefix == other.prefix)) ) return false;

        return true;
    }

    // ==================================================
    
    // Anonymous subclasses of Units which each model a single Unit
    
    /* for Type hierarchy */
    /* Top of qualifier hierarchy */
    protected static final Units UNITSUNKNOWN = new Units("Unknown", UnknownUnits.class) {
        @Override
        public String toString() {
            return "UnitsUnknown";
        }
    };

    /* bottom of hierarchy */
    protected static final Units BOTTOM = new Units("Bottom", UnitsBottom.class)
    {
        @Override
        public String toString() {
            return "UnitsBottom";
        }
    };
    /*
    protected static final Units MULTIPLE = new Units()
    {
        @Override
        public String toString() {
            return "UnitsMultiple";
        }
    };
     */
    /* for when two units are mixed together without a defined SI unit
     * eg meters + second */
    protected static final Units MIXED = new Units("Mixed", UNITSUNKNOWN, MixedUnits.class)
    {
        @Override
        public String toString() {
            return "UnitsMixed";
        }
    };

    // Actual Units ===============================

    // Acceleration
    public static final Units Acceleration = new Units("Acceleration", UNITSUNKNOWN, Acceleration.class) {};
    public static final Units mPERs2 = new Units("mPERs2", Acceleration, mPERs2.class) {};

    // Angle
    public static final Units Angle = new Units("Angle", UNITSUNKNOWN, Angle.class) {};
    public static final Units degrees = new Units("degrees", Angle, degrees.class) {};
    public static final Units radians = new Units("radians", Angle, radians.class) {};

    // Area
    public static final Units Area = new Units("Area", UNITSUNKNOWN, Area.class) {};
    public static final Units km2 = new Units("m2", Prefix.kilo, Area, km2.class) {};
    public static final Units m2 = new Units("m2", Area, m2.class) {};
    public static final Units mm2 = new Units("m2", Prefix.milli, Area, mm2.class) {};

    // Current
    public static final Units Current = new Units("Current", UNITSUNKNOWN, Current.class) {};
    public static final Units A = new Units("Ampere", Current, A.class) {};

    // Temperature
    public static final Units Temperature = new Units("Temperature", UNITSUNKNOWN, Temperature.class) {};
    public static final Units C = new Units("Celsius", Temperature, C.class) {};
    public static final Units K = new Units("Kelvin", Temperature, K.class) {};

    // Time
    public static final Units Time = new Units("Time", UNITSUNKNOWN, Time.class) {};
    public static final Units h = new Units("hour", Time, h.class) {};
    public static final Units min = new Units("minute", Time, min.class) {};
    public static final Units s = new Units("second", Time, s.class) {};

    // Luminance
    public static final Units Luminance = new Units("Luminance", UNITSUNKNOWN, Luminance.class) {};
    public static final Units cd = new Units("candela", Luminance, cd.class) {};

    // Length
    public static final Units Length = new Units("Length", UNITSUNKNOWN, Length.class) {};
    public static final Units km = new Units("m", Prefix.kilo, Length, km.class) {};
    public static final Units m = new Units("m", Length, m.class) {};
    public static final Units mm = new Units("m", Prefix.milli, Length, mm.class) {};

    //Mass
    public static final Units Mass = new Units("Mass", UNITSUNKNOWN, Mass.class) {};
    public static final Units g = new Units("g", Mass, g.class) {};
    public static final Units kg = new Units("g", Prefix.kilo, Mass, kg.class) {};
    //public static final Units mg = new Units("g", Prefix.milli, Mass, null) {}; // no existing annotation

    // Speed
    public static final Units Speed = new Units("Speed", UNITSUNKNOWN, Speed.class) {};
    public static final Units mPERs = new Units("mPERs", Speed, mPERs.class) {};
    public static final Units kmPERh = new Units("kmPERh", Speed, kmPERh.class) {};

    // Substance
    public static final Units Substance = new Units("Substance", UNITSUNKNOWN, Substance.class) {};
    public static final Units mol = new Units("mol", Substance, mol.class) {};

}
