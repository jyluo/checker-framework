package org.checkerframework.checker.experimental.units_qual_poly;

import org.checkerframework.checker.units.qual.Prefix;

// TODO : add some kind of way to ensure that there are no two units with the same name and prefix, perhaps by adapting this into a factory-pool

// TODO 2 : subclass units like mPREs2 into plural Units so that users can define their own custom mixed units that are based upon a mathematical definition (eg x/y, x^n)

public abstract class Units {
    //In the Units system, each unit can have at most one super type.
    protected final Units superType;
    protected final String unitName;
    protected final Prefix prefix;

    // constructor for units which have no defined super type, and no defined prefix
    // by default they have UnitsUnknown as their super type, and prefix of Prefix.one
    public Units(String name) {
        superType = null;
        unitName = name;
        prefix = Prefix.one;
    }

    // constructor for units which have no defined super type, by default they have UnitsUnknown as their super type
    public Units(String name, Prefix p) {
        superType = null;
        unitName = name;
        prefix = p;
    }

    // constructor for units with a defined super type but no defined prefix, by default it will have a prefix of Prefix.one
    public Units(String name, Units superUnit) {
        superType = null;
        unitName = name;
        prefix = Prefix.one;
    }

    // constructor for units with a defined super type
    public Units(String name, Prefix p, Units superUnit) {
        superType = superUnit;
        unitName = name;
        prefix = p;
    }

    @Override
    public String toString() {
        return "Units(" + unitName + ", prefix = " + prefix +")";
    }

    
    public Units getSuperType() {
        return superType;
    }
    
    public String getName() {
        return unitName;
    }
    
    public Prefix getPrefix() {
        return prefix;
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
        if(this.equals(UnitsUnknown))
            return false;
        // everything else is always a sub type of UnitsUnknown
        if(unit.equals(UnitsUnknown))
            return true;
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

    /* for Type hierarchy */
    /* Top of qualifier hierarchy */
    public static final Units UnitsUnknown = new Units("Unknown") {
        @Override
        public String toString() {
            return "UnitsUnknown";
        }
    };

    /* bottom of hierarchy */
    protected static final Units BOTTOM = new Units("Bottom")
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
    protected static final Units MIXED = new Units("Mixed", UnitsUnknown)
    {
        @Override
        public String toString() {
            return "UnitsMixed";
        }
    };

    // Actual Units ===============================

    // Acceleration
    public static final Units Acceleration = new Units("Acceleration", UnitsUnknown) {};
    public static final Units mPERs2 = new Units("mPERs2", Acceleration) {};

    // Angle
    public static final Units Angle = new Units("Angle", UnitsUnknown) {};
    public static final Units degrees = new Units("degrees", Angle) {};
    public static final Units radians = new Units("radians", Angle) {};

    // Area
    public static final Units Area = new Units("Area", UnitsUnknown) {};
    public static final Units km2 = new Units("m2", Prefix.kilo, Area) {};
    public static final Units m2 = new Units("m2", Area) {};
    public static final Units mm2 = new Units("m2", Prefix.milli, Area) {};

    // Current
    public static final Units Current = new Units("Current", UnitsUnknown) {};
    public static final Units A = new Units("Ampere", Current) {};

    // Temperature
    public static final Units Temperature = new Units("Temperature", UnitsUnknown) {};
    public static final Units C = new Units("Celsius", Temperature) {};
    public static final Units K = new Units("Kelvin", Temperature) {};

    // Time
    public static final Units Time = new Units("Time", UnitsUnknown) {};
    public static final Units h = new Units("hour", Time) {};
    public static final Units min = new Units("minute", Time) {};
    public static final Units s = new Units("second", Time) {};

    // Luminance
    public static final Units Luminance = new Units("Luminance", UnitsUnknown) {};
    public static final Units cd = new Units("candela", Luminance) {};

    // Length
    public static final Units Length = new Units("Length", UnitsUnknown) {};
    public static final Units km = new Units("m", Prefix.kilo, Length) {};
    public static final Units m = new Units("m", Length) {};
    public static final Units mm = new Units("m", Prefix.milli, Length) {};

    //Mass
    public static final Units Mass = new Units("Mass", UnitsUnknown) {};
    public static final Units g = new Units("g", Mass) {};
    public static final Units kg = new Units("g", Prefix.kilo, Mass) {};
    public static final Units mg = new Units("g", Prefix.milli, Mass) {};

    // Speed
    public static final Units Speed = new Units("Speed", UnitsUnknown) {};
    public static final Units mPERs = new Units("mPERs", Speed) {};
    public static final Units kmPERh = new Units("kmPERh", Speed) {};

    // Substance
    public static final Units Substance = new Units("Substance", UnitsUnknown) {};
    public static final Units mol = new Units("mol", Substance) {};


}
