package org.checkerframework.checker.experimental.units_qual_poly;

import org.checkerframework.checker.experimental.units_qual_poly.Units.UnitsBuilder;
import org.checkerframework.checker.experimental.units_qual_poly.qual.ClassUnitParam;
import org.checkerframework.checker.experimental.units_qual_poly.qual.MethodUnitParam;
import org.checkerframework.checker.experimental.units_qual_poly.qual.MultiUnit;
import org.checkerframework.checker.experimental.units_qual_poly.qual.PolyUnit;
import org.checkerframework.checker.experimental.units_qual_poly.qual.Var;
import org.checkerframework.checker.experimental.units_qual_poly.qual.Wild;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;
import org.checkerframework.qualframework.base.Checker;
import org.checkerframework.qualframework.poly.AnnotationConverterConfiguration;
import org.checkerframework.qualframework.poly.CombiningOperation.Lub;
import org.checkerframework.qualframework.poly.PolyQual.GroundQual;
import org.checkerframework.qualframework.poly.PolyQual.QualVar;
import org.checkerframework.qualframework.poly.QualParams;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;
import org.checkerframework.qualframework.util.ExtendedTypeMirror;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.tools.Diagnostic.Kind;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Convert {@link org.checkerframework.checker.Units.qual.Units}
 * annotations into a {@link Units} qualifier with support for
 * PolyUnits, Var, Wild annotations.
 *
 */

// TODO: support adding custom unit annotations not defined by default

public class UnitsAnnotationConverter extends SimpleQualifierParameterAnnotationConverter<Units> {

    private static final Units DEFAULT = Units.UnitsUnknown;

    private final ProcessingEnvironment processingEnv;

    public UnitsAnnotationConverter() {

        // TODO: figure out what to change here

        // needs to have all of the parts, not sure why yet

        super(new AnnotationConverterConfiguration<>(
                new Lub<>(new UnitsQualifierHierarchy()),
                new Lub<>(new UnitsQualifierHierarchy()),
                //TODO: bug report: if a checker has no multi annotation name prefixes, it should still handle null string
                //null, //MultiUnits.class.getPackage().getName() + ".Multi",
                MultiUnit.class.getPackage().getName() + ".Multi",
                new HashSet<>(Arrays.asList(org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())),
                null,
                ClassUnitParam.class, //ClassUnitParam.class,
                MethodUnitParam.class, //MethodUnitParam.class,
                PolyUnit.class,
                Var.class, //Var.class,
                Wild.class, //Wild.class,
                Units.UnitsUnknown,
                Units.BOTTOM,
                Units.UnitsUnknown));

        processingEnv = null;
    }

    public UnitsAnnotationConverter(ProcessingEnvironment pe)
    {
        super(new AnnotationConverterConfiguration<>(
                new Lub<>(new UnitsQualifierHierarchy()),
                new Lub<>(new UnitsQualifierHierarchy()),
                //TODO: bug report: if a checker has no multi annotation name prefixes, it should still handle null string
                //null, //MultiUnits.class.getPackage().getName() + ".Multi",
                MultiUnit.class.getPackage().getName() + ".Multi",
                new HashSet<>(Arrays.asList(org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())),
                null,
                ClassUnitParam.class, //ClassUnitParam.class,
                MethodUnitParam.class, //MethodUnitParam.class,
                PolyUnit.class,
                Var.class, //Var.class,
                Wild.class, //Wild.class,
                Units.UnitsUnknown,
                Units.BOTTOM,
                Units.UnitsUnknown));

        processingEnv = pe;
    }

    /**
     * Convert @Units(prefix) into a Units qualifier.
     */
    @Override
    public Units getQualifier(AnnotationMirror anno) {
        // debug use
        if(processingEnv != null)
            processingEnv.getMessager().printMessage(Kind.NOTE, "annotation name: " + AnnotationUtils.annotationName(anno));

        // first check through the list of supported annotations and see if the annotation is there
        for(Units u : Units.UnitsBuilder.getSupportedUnitsWithAnnotations()) {
            if(processingEnv != null)
                processingEnv.getMessager().printMessage(Kind.NOTE, "anno canon name: " + u.getAnnotation().getCanonicalName());

            if(u.getAnnotation().getCanonicalName().equals(AnnotationUtils.annotationName(anno))) {
                return u;
            }
        }

        // if annotation is not in the existing list, then 

        // TODO: get the Prefix value
        // TODO: construct and return a Units qualifier

        if (AnnotationUtils.annotationName(anno).equals(
                org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())) {

            // TODO: does this work??
            Prefix prefix = AnnotationUtils.getElementValue(anno, "value", Prefix.class, true);
            // return new Units(prefix);
        }
        return null;
    }

    /**
     * Process annotations from the old namespace.
     */
    @Override
    protected QualParams<Units> specialCaseHandle(AnnotationMirror anno) {

        // if it is not a poly unit, then construct it as a ground qual and return it

        // else if it is a poly unit, then construct it as a qual variable

        /*
        if (AnnotationUtils.annotationName(anno).equals(
                org.checkerframework.checker.Units.qual.Units.class.getName())) {

            Integer value = AnnotationUtils.getElementValue(anno, "value", Integer.class, true);
            return new QualParams<>(new GroundQual<Units>(new Units.UnitsVal(value)));

        } else if (AnnotationUtils.annotationName(anno).equals(
                org.checkerframework.checker.Units.qual.PolyUnits.class.getName())) {

            return new QualParams<>(new QualVar<>(POLY_NAME, BOTTOM, TOP));
        }

        ErrorReporter.errorAbort("Unexpected AnnotationMirror found in special case handling: " + anno);

         */
        return null;
    }

    /**
     * This override sets up a polymorphic qualifier when the PolyUnits annotation is used.
     */
    // TODO: see if there's any code changes needed
    @Override
    protected boolean hasPolyAnnotationCheck(ExtendedTypeMirror type) {
        if (type == null) {
            return false;
        }

        for (AnnotationMirror anno : type.getAnnotationMirrors()) {
            if (AnnotationUtils.annotationName(anno).equals(PolyUnit.class.getName())) {
                return true;
            }
        }
        return super.hasPolyAnnotationCheck(type);
    }


}
