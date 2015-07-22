package org.checkerframework.checker.experimental.units_qual_poly;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.checkerframework.checker.experimental.units_qual_poly.qual.PolyUnit;
import org.checkerframework.checker.experimental.units_qual_poly.qual.Prefix;
import org.checkerframework.checker.experimental.units_qual_poly.qual.UnitsMultiple;
import org.checkerframework.checker.experimental.units_qual_poly.qual.g;
import org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual.ClassUnitParam;
import org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual.MethodUnitParam;
import org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual.MultiUnit;
import org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual.Var;
import org.checkerframework.checker.experimental.units_qual_poly.qualAPI_qual.Wild;
import org.checkerframework.checker.units.UnitsRelations;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.qualframework.poly.AnnotationConverterConfiguration;
import org.checkerframework.qualframework.poly.CombiningOperation.Lub;
import org.checkerframework.qualframework.poly.QualParams;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;
import org.checkerframework.qualframework.util.ExtendedTypeMirror;

import com.sun.tools.javac.code.TypeAnnotations.AnnotationType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.tools.Diagnostic.Kind;

/**
 * Convert {@link org.checkerframework.checker.Units.qual.Units}
 * annotations into a {@link Units} qualifier with support for
 * PolyUnits, Var, Wild annotations.
 *
 */

// TODO: support adding custom unit annotations not defined by default

public class UnitsAnnotationConverter extends SimpleQualifierParameterAnnotationConverter<Units> {

    private static final Units DEFAULT = Units.UNITSUNKNOWN;

    private final ProcessingEnvironment processingEnv;

    public UnitsAnnotationConverter() {

        // Load all annotation names by reflection
        // process the annotations and construct a bunch of qualifiers reflecting each annotation's name, base unit, prefix.
        
        

        super(new AnnotationConverterConfiguration<>(
                new Lub<>(new UnitsQualifierHierarchy()),
                new Lub<>(new UnitsQualifierHierarchy()),
                //TODO: bug report: if a checker has no multi annotation name prefixes, it should still handle null string
                //null, //MultiUnits.class.getPackage().getName() + ".Multi",
                MultiUnit.class.getPackage().getName() + ".Multi",
                // Supported Annotation Names
                //new HashSet<>(Arrays.asList(org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())),
                UnitsQualReflectionLoader.getInstance(null).getSupportedAnnotationNames(),
                // Special Case Annotation Names
                null,
                ClassUnitParam.class, //ClassUnitParam.class,
                MethodUnitParam.class, //MethodUnitParam.class,
                PolyUnit.class,
                Var.class, //Var.class,
                Wild.class, //Wild.class,
                Units.UNITSUNKNOWN,
                Units.BOTTOM,
                Units.UNITSUNKNOWN));

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
                // Supported Annotation Names
                //new HashSet<>(Arrays.asList(org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())),
                UnitsQualReflectionLoader.getInstance(pe).getSupportedAnnotationNames(),
                // Special Case Annotation Names
                null,
                ClassUnitParam.class, //ClassUnitParam.class,
                MethodUnitParam.class, //MethodUnitParam.class,
                PolyUnit.class,
                Var.class, //Var.class,
                Wild.class, //Wild.class,
                Units.UNITSUNKNOWN,
                Units.BOTTOM,
                Units.UNITSUNKNOWN));

        processingEnv = pe;
        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Annotation Converter Created");

        //Set<String> annoNames = UnitsQualPool.getSupportedAnnotationNames();

        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Supported Annotations (" + annoNames.size() + "): " + String.join(", ", annoNames));

    }

    /**
     * Convert @Units(prefix) into a Units qualifier.
     */
    @Override
    public Units getQualifier(AnnotationMirror anno) {
        // debug use
        //        if(processingEnv != null) {
        //            processingEnv.getMessager().printMessage(Kind.NOTE, "========================");
        //            processingEnv.getMessager().printMessage(Kind.NOTE, "anno tostring: " + anno.toString());
        //            processingEnv.getMessager().printMessage(Kind.NOTE, "prefix: " + AnnotationUtils.getElementValueEnum(anno, "value", Prefix.class, true));
        //            processingEnv.getMessager().printMessage(Kind.NOTE, "annotation name: " + AnnotationUtils.annotationName(anno));
        //            processingEnv.getMessager().printMessage(Kind.NOTE, "========================");
        //        }

        // get annotation's canonical class name
        String annoName = AnnotationUtils.annotationName(anno);
        // set unit prefix to one by default
        Prefix prefix = null;

        // 3 cases:
        // base case: has no meta-annotation (eg @g int x;), default Prefix is sufficient



        // TODO: extract super type of hierarchy from meta annotation

        // has name, has prefix (eg @g(Prefix.kilo) int y;)
        if(AnnotationUtils.hasElementValue(anno, "value")) {
            prefix = AnnotationUtils.getElementValueEnum(anno, "value", Prefix.class, true);
            //            if(processingEnv != null) {
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "anno & prefix: " + annoName + " || " + prefix);
            //            }
        }
        // has name, no prefix
        else{
            AnnotationMirror metaAnnotation = null;

            // check to see if there's a @MultiUnits meta-annotation
            for (AnnotationMirror am : anno.getAnnotationType().asElement().getAnnotationMirrors() ) {
                if(am.getAnnotationType().toString().equals(UnitsMultiple.class.getCanonicalName())) {
                    metaAnnotation = am;
                    break;
                }
            }

            // has meta-annotation (eg @kg int z;)
            if(metaAnnotation != null)  {
                annoName = AnnotationUtils.getElementValueClassName(metaAnnotation, "quantity", true).toString(); // gets canonical anno name
                prefix = AnnotationUtils.getElementValueEnum(metaAnnotation, "prefix", Prefix.class, true);     // gets meta prefix
                //                if(processingEnv != null) {
                //                    processingEnv.getMessager().printMessage(Kind.NOTE, "anno meta & prefix: " + annoName + " || " + prefix);
                //                }
            }

        }

        // check through the list of supported annotations and see if the annotation is there
        for(Units u : UnitsQualReflectionLoader.getInstance(processingEnv).getSupportedUnits()) {

            //processingEnv.getMessager().printMessage(Kind.NOTE, u.getAnnotation().getTypeName());

            //            if(processingEnv != null && u.getAnnotation().getTypeName().equals(g.class.getCanonicalName())) {
            // canon name returns names like org.checkerframework.checker.experimental.units_qual_poly.qual.mm
            // instead of treating it as a meter annotation 
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "qual: " + u.toString());
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "anno canon name: " + u.getAnnotation().getCanonicalName());
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "  match: " + u.getAnnotation().getCanonicalName().equals(annoName));
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "anno prefix name: " + u.getPrefix() + " target prefix: " + prefix);
            //                processingEnv.getMessager().printMessage(Kind.NOTE, "  match: " + (u.getPrefix() == prefix));
            //processingEnv.getMessager().printMessage(Kind.NOTE, "  match: " + u.getPrefix().toString().equals(prefix.toString()));
            //processingEnv.getMessager().printMessage(Kind.NOTE, "  match: " + Prefix.one.equals(Prefix.one));
            //            }

            if(u.getAnnotation().getCanonicalName().equals(annoName)) {
                Units qual = UnitsQualReflectionLoader.getInstance(processingEnv).getQualifier(u.getUnitName(), prefix);
                //processingEnv.getMessager().printMessage(Kind.NOTE, "Units Qual: " + qual.toString() + " super " + qual.getSuperType());
                return qual;
            }
        }

        // if annotation is not in the existing list, then 

        //        if (AnnotationUtils.annotationName(anno).equals(
        //                org.checkerframework.checker.experimental.units_qual_poly.Units.class.getName())) {
        //
        //            // processingEnv.getMessager().printMessage(Kind.NOTE, "anno " + AnnotationUtils.annotationName(anno) + " slipped!");
        //            // TODO: does this work??
        //            Prefix prefix = AnnotationUtils.getElementValue(anno, "value", Prefix.class, true);
        //            // return new Units(prefix);
        //        }
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
