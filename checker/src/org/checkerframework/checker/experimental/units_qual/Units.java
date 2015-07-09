package org.checkerframework.checker.experimental.units_qual;

public abstract class Units {
    protected final Units superType;
    protected final String unitName;

    // constructor for units which have no defined super type, by default they have UnitsUnknown as their super type
    public Units(String name) {
        unitName = name;
        superType = null;
    }

    // constructor for units with a defined super type
    public Units(String name, Units superUnit) {
        unitName = name;
        superType = superUnit;
    }

    @Override
    public String toString() {
        return "Units_" + unitName;
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
        
        if(this.superType.equals(unit)) {
            
            return true;
        }
        return false;
    }

    /**
     * Compares one unit to another based on their names
     */
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null  || this.getClass() != o.getClass()) return false;

        // two units are equal if their names are the same
        Units unit = (Units) o;
        if(this.toString().equals(unit.toString())) return true;

        return false;
    }

    @Override
    public int hashCode()
    {
        // take the name of the unit and return the string's hashcode as the hashcode of the unit
        return this.toString().hashCode();
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
    public static final Units km2 = new Units("km2", Area) {};
    public static final Units m2 = new Units("m2", Area) {};
    public static final Units mm2 = new Units("mm2", Area) {};

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
    public static final Units km = new Units("km", Length) {};
    public static final Units m = new Units("m", Length) {};
    public static final Units mm = new Units("mm", Length) {};

    //Mass
    public static final Units Mass = new Units("Mass", UnitsUnknown) {};
    public static final Units g = new Units("g", Mass) {};
    public static final Units kg = new Units("kg", Mass) {};
    //public static final Units mg = new Units("mg", Mass) {};

    // Speed
    public static final Units Speed = new Units("Speed", UnitsUnknown) {};
    public static final Units mPERs = new Units("mPERs", Speed) {};
    public static final Units kmPERh = new Units("kmPERh", Speed) {};

    // Substance
    public static final Units Substance = new Units("Substance", UnitsUnknown) {};
    public static final Units mol = new Units("mol", Substance) {};


}
