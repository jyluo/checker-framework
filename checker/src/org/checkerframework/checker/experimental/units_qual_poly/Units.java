package org.checkerframework.checker.experimental.units_qual_poly;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.*;

// TODO : add some kind of way to ensure that there are no two units with the same name and prefix, perhaps by adapting this into a factory-pool

// TODO 2 : subclass units like mPREs2 into plural Units so that users can define their own custom mixed units that are based upon a mathematical definition (eg x/y, x^n)

public abstract class Units {
    //In the Units system, each unit can have at most one super type.
    protected final String unitName;    // name of the Unit
    protected final Prefix prefix;      // prefix of the Unit
    protected final Units superType;    // super Type of the Unit in the qualifier hierarchy
    protected final Class<? extends Annotation> annotation;     // optional: a reference to the classic qualifier annotation

    // Four constructors for creating a Units qualifier based on existing annotations

    // constructor for units which have no defined super type, and no defined prefix
    // by default they have UnitsUnknown as their super type, and prefix of Prefix.one
    public Units(String name, Class<? extends Annotation> anno) {
        unitName = name;
        prefix = Prefix.one;
        superType = Units.UnitsUnknown;
        annotation = anno;
    }

    // constructor for units with no defined super type, by default they have UnitsUnknown as their super type
    public Units(String name, Prefix p, Class<? extends Annotation> anno) {
        unitName = name;
        prefix = p;
        superType = Units.UnitsUnknown;
        annotation = anno;
    }

    // constructor for units with no defined prefix, by default it will have a prefix of Prefix.one
    public Units(String name, Units superUnit, Class<? extends Annotation> anno) {
        unitName = name;
        prefix = Prefix.one;
        superType = superUnit;
        annotation = anno;
    }

    // constructor for units with a defined super type and prefix
    public Units(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
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
        if(this.equals(UnitsUnknown))
            return false;
        // everything else is always a sub type of UnitsUnknown
        if(unit.equals(UnitsUnknown))
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

    // Helper classes ==============================

    // UnitsBuilder used to construct qualifiers programmatically
    public static final class UnitsBuilder extends Units{
        //TODO: store some map of prebuilt qualifiers, if during building it already has one of these qualifiers then return a reference to it instead 

        private static List<Units> supportedUnits;        // singleton

        // Units Qualifier Loader ======================

        public static final List<Units> getSupportedUnits() {
            if(supportedUnits == null) {
                supportedUnits = new ArrayList<Units>();

                Field[] declaredUnits = Units.class.getDeclaredFields();

                for(Field unitField : declaredUnits) {
                    if(     java.lang.reflect.Modifier.isPublic(unitField.getModifiers()) &&
                            java.lang.reflect.Modifier.isStatic(unitField.getModifiers()) && 
                            java.lang.reflect.Modifier.isFinal(unitField.getModifiers()) &&
                            unitField.getType().equals(Units.class)) {

                        try {
                            Units u = (Units) unitField.get(Units.class);

                            supportedUnits.add(u);

                            // System.out.println("Units Qual: " + u.toString());
                        } catch (IllegalArgumentException e) {
                            // TODO better error handling here
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            // TODO better error handling here
                            e.printStackTrace();
                        }
                    }
                }
            } // end if(supportedUnits == null);

            return supportedUnits;
        }

        public static final List<Units> getSupportedUnitsWithAnnotations() {
            List<Units> annoUnitsList = new ArrayList<Units>();

            for(Units u : getSupportedUnits())
            {
                if(u.getAnnotation() != null)
                    annoUnitsList.add(u);
            }

            return annoUnitsList;
        }

        // private constructors for creating a Units qualifier
        // we expose two getQualifier methods for instantiating a new qualifier instead, so that we can maintain a lean memory
        // footprint for the total number of qualifier objects in memory
        private UnitsBuilder(String name, Class<? extends Annotation> anno) {
            super(name, anno);
        }
        private UnitsBuilder(String name, Prefix p, Class<? extends Annotation> anno) {
            super(name, p, anno);
        }
        private UnitsBuilder(String name, Units superUnit, Class<? extends Annotation> anno) {
            super(name, superUnit, anno);
        }
        private UnitsBuilder(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
            super(name, p, superUnit, anno);
        }

        // behaves just like Singleton.getInstance() in concept: checks to see if there's already an existing qualifier with
        // a matching name and prefix. If so it will return a reference to the existing one, if not it will make a new one
        // and add it to the list of qualifiers
        public static final Units getQualifier(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
            for(Units qual : getSupportedUnits()) {
                // if there's already an existing qualifier with the same name and prefix as the one desired, return that one
                if(qual.getUnitName().equals(name) && qual.getPrefix() == p)
                    return qual;
            }

            // otherwise create a new qualifier, add it to the supportedUnits list, then return it
            Units newQual = (Units) new UnitsBuilder(name, p, superUnit, anno);
            getSupportedUnits().add(newQual);
            return newQual;
        }

        public static final Units getQualifier(String name, Prefix p, Units superUnit) {
            return getQualifier(name, p, superUnit, null);
        }
        
        public static final Units getQualifier(String name, Prefix p) {
            return getQualifier(name, p, Units.UnitsUnknown);
        }

        //
        //        // detects whether the desired qualifier has already been created in the supportedUnits list
        //        private static final boolean qualifierExists(String name, Prefix p) {
        //            for(Units qual : supportedUnits) {
        //                if(qual.getUnitName().equals(name) && qual.getPrefix() == p)
        //                    return true;
        //            }
        //            return false;
        //        }
    }

    // =============================================

    /* for Type hierarchy */
    /* Top of qualifier hierarchy */
    protected static final Units UnitsUnknown = new Units("Unknown", UnknownUnits.class) {
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
    protected static final Units MIXED = new Units("Mixed", UnitsUnknown, MixedUnits.class)
    {
        @Override
        public String toString() {
            return "UnitsMixed";
        }
    };

    // Actual Units ===============================

    // Acceleration
    public static final Units Acceleration = new Units("Acceleration", UnitsUnknown, Acceleration.class) {};
    public static final Units mPERs2 = new Units("mPERs2", Acceleration, mPERs2.class) {};

    // Angle
    public static final Units Angle = new Units("Angle", UnitsUnknown, Angle.class) {};
    public static final Units degrees = new Units("degrees", Angle, degrees.class) {};
    public static final Units radians = new Units("radians", Angle, radians.class) {};

    // Area
    public static final Units Area = new Units("Area", UnitsUnknown, Area.class) {};
    public static final Units km2 = new Units("m2", Prefix.kilo, Area, km2.class) {};
    public static final Units m2 = new Units("m2", Area, m2.class) {};
    public static final Units mm2 = new Units("m2", Prefix.milli, Area, mm2.class) {};

    // Current
    public static final Units Current = new Units("Current", UnitsUnknown, Current.class) {};
    public static final Units A = new Units("Ampere", Current, A.class) {};

    // Temperature
    public static final Units Temperature = new Units("Temperature", UnitsUnknown, Temperature.class) {};
    public static final Units C = new Units("Celsius", Temperature, C.class) {};
    public static final Units K = new Units("Kelvin", Temperature, K.class) {};

    // Time
    public static final Units Time = new Units("Time", UnitsUnknown, Time.class) {};
    public static final Units h = new Units("hour", Time, h.class) {};
    public static final Units min = new Units("minute", Time, min.class) {};
    public static final Units s = new Units("second", Time, s.class) {};

    // Luminance
    public static final Units Luminance = new Units("Luminance", UnitsUnknown, Luminance.class) {};
    public static final Units cd = new Units("candela", Luminance, cd.class) {};

    // Length
    public static final Units Length = new Units("Length", UnitsUnknown, Length.class) {};
    public static final Units km = new Units("m", Prefix.kilo, Length, km.class) {};
    public static final Units m = new Units("m", Length, m.class) {};
    public static final Units mm = new Units("m", Prefix.milli, Length, mm.class) {};

    //Mass
    public static final Units Mass = new Units("Mass", UnitsUnknown, Mass.class) {};
    public static final Units g = new Units("g", Mass, g.class) {};
    public static final Units kg = new Units("g", Prefix.kilo, Mass, kg.class) {};
    public static final Units mg = new Units("g", Prefix.milli, Mass, null) {}; // no existing annotation

    // Speed
    public static final Units Speed = new Units("Speed", UnitsUnknown, Speed.class) {};
    public static final Units mPERs = new Units("mPERs", Speed, mPERs.class) {};
    public static final Units kmPERh = new Units("kmPERh", Speed, kmPERh.class) {};

    // Substance
    public static final Units Substance = new Units("Substance", UnitsUnknown, Substance.class) {};
    public static final Units mol = new Units("mol", Substance, mol.class) {};

}
