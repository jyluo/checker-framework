package org.checkerframework.checker.experimental.units_qual_poly;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import org.checkerframework.checker.experimental.units_qual_poly.qual.Prefix;

//UnitsBuilder used to construct qualifiers programmatically
public final class UnitsQualPool{
    //TODO: store some map of prebuilt qualifiers, if during building it already has one of these qualifiers then return a reference to it instead 
    private static UnitsQualPool pool; // singleton pool
    
    private static List<Units> supportedUnits;        // singleton

    private static ProcessingEnvironment processingEnv;
    
    // private constructors for creating a Units qualifier
    // we expose two getQualifier methods for instantiating a new qualifier instead, so that we can maintain a lean memory
    // footprint for the total number of qualifier objects in memory
    //    private UnitsQualPool(String name, Class<? extends Annotation> anno) {
    //        super(name, anno);
    //    }
    //    private UnitsQualPool(String name, Prefix p, Class<? extends Annotation> anno) {
    //        super(name, p, anno);
    //    }
    //    private UnitsQualPool(String name, Units superUnit, Class<? extends Annotation> anno) {
    //        super(name, superUnit, anno);
    //    }
    //    private UnitsQualPool(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
    //        super(name, p, superUnit, anno);
    //    }

    private UnitsQualPool(ProcessingEnvironment pe)
    {
        processingEnv = pe;
        
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
    }

    // singleton pool methods
    public static UnitsQualPool getInstance(ProcessingEnvironment pe) {
        if(pool == null) {
            pool = new UnitsQualPool(pe);
        }
        else {
            processingEnv = pe;
        }

        return pool;
    }

    // Units Qualifier Loader ======================

    public final List<Units> getSupportedUnits() {
        return supportedUnits;
    }

    public final List<Units> getSupportedUnitsWithAnnotations() {
        List<Units> annoUnitsList = new ArrayList<Units>();

        for(Units u : getSupportedUnits())
        {
            if(u.getAnnotation() != null)
                annoUnitsList.add(u);
        }

        return annoUnitsList;
    }

    public final Set<String> getSupportedAnnotationNames() {
        //new HashSet<>(Arrays.asList(org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())),
        HashSet<String> annoNames = new HashSet<String>();

        for(Units u : getSupportedUnitsWithAnnotations()) {
            annoNames.add(u.getAnnotation().getName());
        }

        return annoNames;
    }

    // behaves just like Singleton.getInstance() in concept: checks to see if there's already an existing qualifier with
    // a matching name and prefix. If so it will return a reference to the existing one, if not it will make a new one
    // and add it to the list of qualifiers
    public final Units getQualifier(String name, Prefix p, Units superUnit, Class<? extends Annotation> anno) {
        Units targetQual = null;

        // ensure p is never null
        if(p == null)
            p = Prefix.one;
        
        // loop through all existing qualifiers in the pool
        for(Units qual : getSupportedUnits()) {
            // see if there's an existing qualifier that matches the desired qualifier name
            if(qual.getUnitName().equals(name)) {
                // if its prefix also matches, then return that unit
                if(qual.getPrefix() == p) {
//                    if(processingEnv != null)
//                        processingEnv.getMessager().printMessage(Kind.NOTE, "=== Existing Unit ===: " + qual.toString());
                    return qual;
                }
                // otherwise store this qual as a target qual, where the new one constructed share's the same super type as this qual
                else {
                    targetQual = qual;
                }
            }
        }

        // if there's a super type passed in, then create a brand new unit qualifier
        if(superUnit != null) {
            Units brandNewQual = new Units(name, p, superUnit, anno);
            getSupportedUnits().add(brandNewQual);
//            if(processingEnv != null)
//                processingEnv.getMessager().printMessage(Kind.NOTE, "=== New Unit with custom super ===: " + brandNewQual.toString() + "==================================");
            return brandNewQual;
        }
        // otherwise, if there's an existing unit that has the same name but different prefix (as detected earlier), 
        // create a new qualifier with the existing unit's super type
        else if(targetQual != null) {
            Units qualWithNewPrefix = new Units(name, p, targetQual.getSuperType(), anno);
            getSupportedUnits().add(qualWithNewPrefix);
//            if(processingEnv != null)
//                processingEnv.getMessager().printMessage(Kind.NOTE, "=== New Unit with existing super ===: " + qualWithNewPrefix.toString() + "==================================");
            return qualWithNewPrefix;
        }
        else {
            // otherwise create a new qualifier, add it to the supportedUnits list, then return it
            Units newUpperQual = new Units(name, p, Units.UNITSUNKNOWN, anno);
            getSupportedUnits().add(newUpperQual);
//            if(processingEnv != null)
//                processingEnv.getMessager().printMessage(Kind.NOTE, "=== New Unit with default super ===: " + newUpperQual.toString() + "==================================");
            return newUpperQual;
        }
    }

    public final Units getQualifier(String name, Prefix p, Units superUnit) {
        return getQualifier(name, p, superUnit, null);
    }

    public final Units getQualifier(String name, Prefix p) {
        return getQualifier(name, p, null);
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
