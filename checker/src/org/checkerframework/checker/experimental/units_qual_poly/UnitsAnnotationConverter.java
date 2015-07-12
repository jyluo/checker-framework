package org.checkerframework.checker.experimental.units_qual_poly;

import org.checkerframework.checker.experimental.Units_qual.Units;
import org.checkerframework.checker.experimental.Units_qual.UnitsQualifierHierarchy;
import org.checkerframework.checker.units.qual.PolyUnit;
import org.checkerframework.checker.Units.qual.ClassUnitsParam;
import org.checkerframework.checker.Units.qual.MethodUnitsParam;
import org.checkerframework.checker.Units.qual.MultiUnits;
import org.checkerframework.checker.Units.qual.Var;
import org.checkerframework.checker.Units.qual.Wild;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;
import org.checkerframework.qualframework.poly.AnnotationConverterConfiguration;
import org.checkerframework.qualframework.poly.CombiningOperation.Lub;
import org.checkerframework.qualframework.poly.PolyQual.GroundQual;
import org.checkerframework.qualframework.poly.PolyQual.QualVar;
import org.checkerframework.qualframework.poly.QualParams;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;
import org.checkerframework.qualframework.util.ExtendedTypeMirror;

import javax.lang.model.element.AnnotationMirror;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Convert {@link org.checkerframework.checker.Units.qual.Units}
 * annotations into a {@link Units} qualifier with support for
 * PolyUnits, Var, Wild annotations.
 *
 */
public class UnitsAnnotationConverter extends SimpleQualifierParameterAnnotationConverter<Units> {

    public UnitsAnnotationConverter() {
        
        // TODO: figure out what to change here
        
        super(new AnnotationConverterConfiguration<Units>(
                new Lub<>(new UnitsQualifierHierarchy()),
                new Lub<>(new UnitsQualifierHierarchy()),
                MultiUnits.class.getPackage().getName() + ".Multi",
                new HashSet<>(Arrays.asList(org.checkerframework.checker.units.qual.Units.class.getName())),
                null,
                ClassUnitsParam.class,
                MethodUnitsParam.class,
                PolyUnit.class,
                Var.class,
                Wild.class,
                Units.UnitsUnknown,
                Units.BOTTOM,
                Units.UnitsUnknown));
    }

    /**
     * Convert @Units(prefix) into a Units qualifier.
     */
    @Override
    public Units getQualifier(AnnotationMirror anno) {
        
        // if annotation is one of the supported Units annotations, then
        
        // get the Prefix value
        
        // construct and return a Units qualifier
        
        // Support methods needed:
        // - checking to see if annotation is supported
        // - constructing a Units qualifier (nicer to pass in class name and prefix)
        
        
        if (AnnotationUtils.annotationName(anno).equals(
                org.checkerframework.checker.units.qual.Units.class.getName())) {

            Integer value = AnnotationUtils.getElementValue(anno, "value", Integer.class, true);
            return new Units.UnitsVal(value);
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
        
        
        if (AnnotationUtils.annotationName(anno).equals(
                org.checkerframework.checker.Units.qual.Units.class.getName())) {

            Integer value = AnnotationUtils.getElementValue(anno, "value", Integer.class, true);
            return new QualParams<>(new GroundQual<Units>(new Units.UnitsVal(value)));

        } else if (AnnotationUtils.annotationName(anno).equals(
                org.checkerframework.checker.Units.qual.PolyUnits.class.getName())) {

            return new QualParams<>(new QualVar<>(POLY_NAME, BOTTOM, TOP));
        }

        ErrorReporter.errorAbort("Unexpected AnnotationMirror found in special case handling: " + anno);
        return null;
    }

    /**
     * This override sets up a polymorphic qualifier when the old PolyUnits annotation is used.
     */
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
