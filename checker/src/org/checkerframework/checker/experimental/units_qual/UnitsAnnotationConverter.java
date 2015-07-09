package org.checkerframework.checker.experimental.units_qual;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.qualframework.base.AnnotationConverter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.checkerframework.checker.units.qual.*;

/**
 * Convert {@link org.checkerframework.checker.units.qual}
 * annotations into a {@link Units} qualifier.
 */
public class UnitsAnnotationConverter implements AnnotationConverter<Units> {
    // Acceleration
    private static final String AccelerationName = Acceleration.class.getName();
    private static final String mPERs2Name = mPERs2.class.getName();

    // Angle
    private static final String AngleName = Angle.class.getName();
    private static final String degreesName = degrees.class.getName();
    private static final String radiansName = radians.class.getName();

    // Area
    private static final String AreaName = Area.class.getName();
    private static final String km2Name = km2.class.getName();
    private static final String m2Name = m2.class.getName();
    private static final String mm2Name = mm2.class.getName();

    // Current
    private static final String CurrentName = Current.class.getName();
    private static final String AmpereName = A.class.getName();

    // Temperature
    private static final String TemperatureName = Temperature.class.getName();
    private static final String CelsiusName = C.class.getName();
    private static final String KelvinName = K.class.getName();

    // Time
    private static final String TimeName = Time.class.getName();
    private static final String secName = s.class.getName();
    private static final String minName = min.class.getName();
    private static final String hourName = h.class.getName();

    // Luminance
    private static final String LuminanceName = Luminance.class.getName();
    private static final String cdName = cd.class.getName();

    // Length
    private static final String LengthName = Length.class.getName();
    private static final String kmName = km.class.getName();
    private static final String mName = m.class.getName();
    private static final String mmName = mm.class.getName();

    // Mass
    private static final String MassName = Mass.class.getName();
    private static final String kgName = kg.class.getName();
    private static final String gName = g.class.getName();
    // private static final String mgName = mg.class.getName();

    // Speed
    private static final String SpeedName = Speed.class.getName();
    private static final String kmPERhName = kmPERh.class.getName();
    private static final String mPERsName = mPERs.class.getName();

    // Substance
    private static final String SubstanceName = Substance.class.getName();
    private static final String molName = mol.class.getName();

    // TODO: add in all metric-prefixed SI unit annotations

    private static final Units DEFAULT = Units.UnitsUnknown;

    private ProcessingEnvironment processingEnv;

    // Helper class to store a single mapping between an annotation's class name, a prefix, and the corresponding
    // qual framework Units qualifier
    private class AnnotationUnitsTuple {
        private String annotation;      // stores the class name of an annotation
        private Prefix prefix;          // stores the SI prefix
        private Units unit;             // stores the qual framework Units qualifier

        public AnnotationUnitsTuple(String anno, Prefix p, Units u) {
            annotation = anno;
            prefix = p;
            unit = u;
        }

        public String getAnnotationName() {
            return annotation;
        }

        public Prefix getPrefix() {
            return prefix;
        }

        public Units getUnit() {
            return unit;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result
                    + ((annotation == null) ? 0 : annotation.hashCode());
            result = prime * result
                    + ((prefix == null) ? 0 : prefix.hashCode());
            result = prime * result + ((unit == null) ? 0 : unit.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AnnotationUnitsTuple other = (AnnotationUnitsTuple) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (annotation == null) {
                if (other.annotation != null)
                    return false;
            } else if (!annotation.equals(other.annotation))
                return false;
            if (prefix != other.prefix)
                return false;
            if (unit == null) {
                if (other.unit != null)
                    return false;
            } else if (!unit.equals(other.unit))
                return false;
            return true;
        }

        private UnitsAnnotationConverter getOuterType() {
            return UnitsAnnotationConverter.this;
        }
    }

    private ArrayList<AnnotationUnitsTuple> annoUnitsList;

    private ArrayList<String> supportedAnnotationNames;

    public UnitsAnnotationConverter(ProcessingEnvironment pe) {
        processingEnv = pe;

        annoUnitsList = new ArrayList<AnnotationUnitsTuple>();
        supportedAnnotationNames = new ArrayList<String>();

        addAnnotationsUnits();

        addSupportedAnnotations();
    }


    private void addAnnotationsUnits(){
        // Adding all versions of each unit to the list
        // Acceleration
        annoUnitsList.add(new AnnotationUnitsTuple(AccelerationName, Prefix.one, Units.Acceleration));
        annoUnitsList.add(new AnnotationUnitsTuple(mPERs2Name, Prefix.one, Units.mPERs2));

        // Angle
        annoUnitsList.add(new AnnotationUnitsTuple(AngleName, Prefix.one, Units.Angle));
        annoUnitsList.add(new AnnotationUnitsTuple(degreesName, Prefix.one, Units.degrees));
        annoUnitsList.add(new AnnotationUnitsTuple(radiansName, Prefix.one, Units.radians));

        // Area
        annoUnitsList.add(new AnnotationUnitsTuple(AreaName, Prefix.one, Units.Area));
        annoUnitsList.add(new AnnotationUnitsTuple(km2Name, Prefix.one, Units.km2));
        annoUnitsList.add(new AnnotationUnitsTuple(m2Name, Prefix.one, Units.m2));
        annoUnitsList.add(new AnnotationUnitsTuple(mm2Name, Prefix.one, Units.mm2));

        // Current
        annoUnitsList.add(new AnnotationUnitsTuple(CurrentName, Prefix.one, Units.Current));
        annoUnitsList.add(new AnnotationUnitsTuple(AmpereName, Prefix.one, Units.A));

        // Temperature
        annoUnitsList.add(new AnnotationUnitsTuple(TemperatureName, Prefix.one, Units.Temperature));
        annoUnitsList.add(new AnnotationUnitsTuple(CelsiusName, Prefix.one, Units.C));
        annoUnitsList.add(new AnnotationUnitsTuple(KelvinName, Prefix.one, Units.K));

        // Time
        annoUnitsList.add(new AnnotationUnitsTuple(TimeName, Prefix.one, Units.Time));
        annoUnitsList.add(new AnnotationUnitsTuple(secName, Prefix.one, Units.s));
        annoUnitsList.add(new AnnotationUnitsTuple(minName, Prefix.one, Units.min));
        annoUnitsList.add(new AnnotationUnitsTuple(hourName, Prefix.one, Units.h));

        // Luminance
        annoUnitsList.add(new AnnotationUnitsTuple(LuminanceName, Prefix.one, Units.Luminance));
        annoUnitsList.add(new AnnotationUnitsTuple(cdName, Prefix.one, Units.cd));

        // Length
        annoUnitsList.add(new AnnotationUnitsTuple(LengthName, Prefix.one, Units.Length));
        // km
        annoUnitsList.add(new AnnotationUnitsTuple(kmName, Prefix.one, Units.km));
        annoUnitsList.add(new AnnotationUnitsTuple(mName, Prefix.kilo, Units.km));
        // m
        annoUnitsList.add(new AnnotationUnitsTuple(mName, Prefix.one, Units.m));
        // mm
        annoUnitsList.add(new AnnotationUnitsTuple(mmName, Prefix.one, Units.mm));
        annoUnitsList.add(new AnnotationUnitsTuple(mName, Prefix.milli, Units.mm));

        // Mass
        annoUnitsList.add(new AnnotationUnitsTuple(MassName, Prefix.one, Units.Mass));
        // kg
        annoUnitsList.add(new AnnotationUnitsTuple(gName, Prefix.kilo, Units.kg));
        annoUnitsList.add(new AnnotationUnitsTuple(kgName, Prefix.one, Units.kg));
        // g
        annoUnitsList.add(new AnnotationUnitsTuple(gName, Prefix.one, Units.g));
        /*
        // mg
        annoUnitsList.add(new AnnotationUnitsTuple(mgName, Prefix.one, Units.mg));
        annoUnitsList.add(new AnnotationUnitsTuple(gName, Prefix.milli, Units.mg));
         */

        // Speed
        annoUnitsList.add(new AnnotationUnitsTuple(SpeedName, Prefix.one, Units.Speed));
        annoUnitsList.add(new AnnotationUnitsTuple(kmPERhName, Prefix.one, Units.kmPERh));
        annoUnitsList.add(new AnnotationUnitsTuple(mPERsName, Prefix.one, Units.mPERs));

        // Substance
        annoUnitsList.add(new AnnotationUnitsTuple(SubstanceName, Prefix.one, Units.Substance));
        annoUnitsList.add(new AnnotationUnitsTuple(molName, Prefix.one, Units.mol));

        // MixedUnits //TODO: is this convertible?
        //annoUnitsList.add(new AnnotationUnitsTuple(MixedUnitsName, Prefix.one, Units.MixedUnits));
    }

    private void addSupportedAnnotations() {
        // Adding all units to the list
        // Acceleration
        supportedAnnotationNames.add(AccelerationName);
        supportedAnnotationNames.add(mPERs2Name);

        // Angle
        supportedAnnotationNames.add(AngleName);
        supportedAnnotationNames.add(degreesName);
        supportedAnnotationNames.add(radiansName);

        // Area
        supportedAnnotationNames.add(AreaName);
        supportedAnnotationNames.add(km2Name);
        supportedAnnotationNames.add(m2Name);
        supportedAnnotationNames.add(mm2Name);

        // Current
        supportedAnnotationNames.add(CurrentName);
        supportedAnnotationNames.add(AmpereName);

        // Temperature
        supportedAnnotationNames.add(TemperatureName);
        supportedAnnotationNames.add(CelsiusName);
        supportedAnnotationNames.add(KelvinName);

        // Time
        supportedAnnotationNames.add(TimeName);
        supportedAnnotationNames.add(secName);
        supportedAnnotationNames.add(minName);
        supportedAnnotationNames.add(hourName);

        // Luminance
        supportedAnnotationNames.add(LuminanceName);
        supportedAnnotationNames.add(cdName);

        // Length
        supportedAnnotationNames.add(LengthName);
        supportedAnnotationNames.add(kmName);
        supportedAnnotationNames.add(mName);
        supportedAnnotationNames.add(mmName);

        // Mass
        supportedAnnotationNames.add(MassName);
        supportedAnnotationNames.add(kgName);
        supportedAnnotationNames.add(gName);
        //supportedAnnotationNames.add(mgName);

        // Speed
        supportedAnnotationNames.add(SpeedName);
        supportedAnnotationNames.add(kmPERhName);
        supportedAnnotationNames.add(mPERsName);

        // Substance
        supportedAnnotationNames.add(SubstanceName);
        supportedAnnotationNames.add(molName);

        // MixedUnits //TODO: is this convertible?
        //annoUnitsList.add(new AnnotationUnitsTuple(MixedUnitsName, Prefix.one, Units.MixedUnits));
    }


    /** If annotated with @someunit, create a someunit qualifier. **/
    @Override
    public Units fromAnnotations(Collection<? extends AnnotationMirror> annos) {
        for(AnnotationMirror anno : annos){


            String annoName = AnnotationUtils.annotationName(anno);
            Prefix annoPrefix = getAnnoPrefix(anno);

            //processingEnv.getMessager().printMessage(Kind.NOTE, annoName);

            //TODO: find a way to do this through the annotation definition instead of hard coding it here
            //TODO: extension support for custom units annotations

            for(AnnotationUnitsTuple annoTuple : annoUnitsList) {
                if(annoName.equals(annoTuple.getAnnotationName()) && annoPrefix.equals(annoTuple.getPrefix())) {
                    return annoTuple.getUnit();
                }
            }

            /*

            // kg
            if(annoPrefix.equals(Prefix.kilo) && annoName.equals(gName) || annoName.equals(kgName)) {
                return Units.kg;
            }
            // mg
            else if(annoName.equals(gName) && getAnnoPrefix(anno).equals(Prefix.milli) || annoName.equals(mgName)){
                return Units.mg;
            }
            else if(annoName.equals(gName)) {
                return Units.g;
            }
            // km
            else if(annoPrefix.equals(Prefix.kilo) && annoName.equals(mName) || annoName.equals(kmName)) {
                return Units.km;
            }
            // mm
            else if(annoPrefix.equals(Prefix.milli) && annoName.equals(mName) || annoName.equals(mmName)) {
                return Units.mm;
            }
            // m
            else if(annoName.equals(mName)) {
                return Units.m;
            }
            // s
            else if(annoName.equals(sName)){
                return Units.s;
            }
            // min
            else if(annoName.equals(minName)){
                return Units.min;
            }
            // h
            else if(annoName.equals(hName)){
                return Units.h;
            }
             */

        }

        return DEFAULT;
    }

    @Override
    public boolean isAnnotationSupported(AnnotationMirror anno) {
        // check to see if anno is any of the supported annotations, return true if yes
        String annoName = AnnotationUtils.annotationName(anno);

        for(String supportedNames : supportedAnnotationNames) {
            if(annoName.equals(supportedNames))
                return true;
        }

        /*
        if(     annoName.equals(gName) ||
                annoName.equals(kgName) ||
                annoName.equals(mName) ||
                annoName.equals(kmName) ||
                annoName.equals(sName)||
                annoName.equals(hName)
                ) {
            return true;
        }
         */

        return false;
    }

    // helper methods
    /*
    // go through each annotation of an annotated type, find the prefix and return it
    private Prefix getTypeMirrorPrefix(AnnotatedTypeMirror atm) {
        for (AnnotationMirror mirror : atm.getAnnotations()) {
            AnnotationValue annotationValue = getAnnotationMirrorPrefix(mirror);
            // annotation has no element value (ie no SI prefix)
            if (annotationValue == null) {
                return Prefix.one;
            }

            // if the annotation has a value, then detect the string name of the prefix and return the Prefix
            String prefixString = annotationValue.getValue().toString();
            for(Prefix p : Prefix.values()) {
                if(prefixString.equals(p.toString())) {
                    return p;
                }
            }
        }
        return null;
    }

    private Prefix getAnnotationMirrorPrefixString(AnnotationMirror mirror) {
        AnnotationValue annotationValue = getAnnotationMirrorPrefix(mirror);
        // if the annotation has an element value (ie SI prefix)
        if (annotationValue != null) {
            // then detect the string name of the prefix and return the Prefix
            String prefixString = annotationValue.getValue().toString();
            for(Prefix p : Prefix.values()) {
                if(prefixString.equals(p.toString())) {
                    return p;
                }
            }
        }
        // else by default return prefix 1
        return Prefix.one;
    }

    // given an annotation, returns the prefix value (eg kilo) if there is any, otherwise returns null
    private AnnotationValue getAnnotationMirrorPrefix(AnnotationMirror mirror) {
        //Map<? extends ExecutableElement,? extends AnnotationValue> elementValues = mirror.getElementValues();
        Map<? extends ExecutableElement,? extends AnnotationValue> elementValues = AnnotationUtils.getElementValuesWithDefaults(mirror);

        for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals("value")){
                return entry.getValue();
            }
        }
        return null;
    }
     */

    private Prefix getAnnoPrefix(AnnotationMirror mirror) {
        AnnotationValue annotationValue = null;

        Map<? extends ExecutableElement,? extends AnnotationValue> elementValues = AnnotationUtils.getElementValuesWithDefaults(mirror);

        for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals("value")){
                annotationValue = entry.getValue();
                break;
            }
        }

        // if the annotation has an element value (ie SI prefix)
        if (annotationValue != null) {
            // then detect the string name of the prefix and return the Prefix
            String prefixString = annotationValue.getValue().toString();
            for(Prefix p : Prefix.values()) {
                if(prefixString.equals(p.toString())) {
                    return p;
                }
            }
        }

        // else by default return prefix 1
        return Prefix.one;
    }


}
