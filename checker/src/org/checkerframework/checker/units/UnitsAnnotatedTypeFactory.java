package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.units.qual.*;
import org.checkerframework.checker.units.qual.time.duration.TimeDuration;
import org.checkerframework.checker.units.qual.time.instant.TimeInstant;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.DefaultAnnotatedTypeFormatter;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
 */

/**
 * Annotated type factory for the Units Checker.
 *
 * <p>Handles multiple names for the same unit, with different prefixes, e.g. @kg is the same
 * as @g(Prefix.kilo).
 *
 * <p>Supports relations between units, e.g. if "m" is a variable of type "@m" and "s" is a variable
 * of type "@s", the division "m/s" is automatically annotated as "mPERs", the correct unit for the
 * result.
 */
public class UnitsAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    protected final AnnotationMirror scalar =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, Scalar.class);
    protected final AnnotationMirror TOP =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, UnknownUnits.class);
    protected final AnnotationMirror BOTTOM =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, UnitsBottom.class);

    protected final AnnotationMirror m =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, m.class);
    protected final AnnotationMirror mm =
            UnitsRelationsTools.buildAnnoMirrorWithSpecificPrefix(
                    processingEnv, m.class, Prefix.milli);
    protected final AnnotationMirror km =
            UnitsRelationsTools.buildAnnoMirrorWithSpecificPrefix(
                    processingEnv, m.class, Prefix.kilo);

    protected final AnnotationMirror m2 =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, m2.class);
    protected final AnnotationMirror mm2 =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, mm2.class);
    protected final AnnotationMirror km2 =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, km2.class);

    protected final AnnotationMirror m3 =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, m3.class);
    protected final AnnotationMirror km3 =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, km3.class);
    protected final AnnotationMirror mm3 =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, mm3.class);

    protected final AnnotationMirror timeDuration =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, TimeDuration.class);;
    protected final AnnotationMirror timeInstant =
            UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, TimeInstant.class);;

    // used to detect and skip string addition processing
    protected final TypeMirror stringType = getTypeMirror(java.lang.String.class);

    // used to handle the loading of external and internally defined
    // UnitsRelations classes
    private static UnitsRelationsManager unitsRelations;

    // used to check arithmetic, compound assignment, and comparison operations
    private static UnitsMathOperatorsRelations mathOpRelations;

    // map of externally loaded qualifiers
    private static final Map<String, Class<? extends Annotation>> externalQualsMap =
            new HashMap<>();

    // map of alias annotations
    private static final Map<String, AnnotationMirror> aliasMap = new HashMap<>();

    // cache of examined type mirror equality comparisons, used in support of
    // identifying the appropriate class processor to provide special processing
    // of method invocations based on its receiver class type
    private static final Map<String, Map<String, Boolean>> typeMirrorSameCache = new HashMap<>();

    public UnitsAnnotatedTypeFactory(BaseTypeChecker checker) {
        // use flow inference
        super(checker, true);

        mathOpRelations = new UnitsMathOperatorsRelations((UnitsChecker) checker, this);

        this.postInit();
    }

    // In Units Checker, we always want to format the print out of qualifiers by removing Prefix.one
    @Override
    protected AnnotatedTypeFormatter createAnnotatedTypeFormatter() {
        return new DefaultAnnotatedTypeFormatter(
                new UnitsAnnotationFormatter(checker),
                checker.hasOption("printVerboseGenerics"),
                checker.hasOption("printAllQualifiers"));
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        // Use the Units Annotated Type Loader instead of the default one
        loader = new UnitsAnnotationClassLoader(checker);

        // during the loading of external annotations, we may run into
        // externally defined units relations classes. Instantiate the manager
        // here to support the loading of external relations classes
        createUnitsRelationsManager();

        // get all the loaded units annotations that are bundled with the units
        // checker
        Set<Class<? extends Annotation>> qualSet = new HashSet<>();
        qualSet.addAll(getBundledTypeQualifiersWithPolyAll());

        // load all the external units
        loadAllExternalUnits();

        // copy all loaded external Units to qual set
        qualSet.addAll(externalQualsMap.values());

        return qualSet;
    }

    private void loadAllExternalUnits() {
        // load external individually named units
        String qualNames = checker.getOption("units");
        if (qualNames != null) {
            for (String qualName : qualNames.split(",")) {
                loadExternalUnit(qualName);
            }
        }

        // load external directories of units
        String qualDirectories = checker.getOption("unitsDirs");
        if (qualDirectories != null) {
            for (String directoryName : qualDirectories.split(":")) {
                loadExternalDirectory(directoryName);
            }
        }
    }

    /** Loads and processes a single external units qualifier. */
    private void loadExternalUnit(String annoName) {
        Class<? extends Annotation> annoClass = loader.loadExternalAnnotationClass(annoName);

        addUnitToExternalQualMap(annoClass);
    }

    /** Loads and processes the units qualifiers from a single external directory. */
    private void loadExternalDirectory(String directoryName) {
        Set<Class<? extends Annotation>> annoClassSet =
                loader.loadExternalAnnotationClassesFromDirectory(directoryName);

        for (Class<? extends Annotation> annoClass : annoClassSet) {
            addUnitToExternalQualMap(annoClass);
        }
    }

    /** Adds the annotation class to the external qualifier map if it is not an alias annotation. */
    private void addUnitToExternalQualMap(final Class<? extends Annotation> annoClass) {
        AnnotationMirror mirror =
                UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, annoClass);

        if (!isAliasedAnnotation(mirror)) {
            // if it is not an aliased annotation, add to external quals map if
            // it isn't already in map
            String unitClassName = annoClass.getCanonicalName().intern();
            if (!externalQualsMap.containsKey(unitClassName)) {
                externalQualsMap.put(unitClassName, annoClass);
            }
        } else {
            // if it is an aliased annotation
            Pair<Class<? extends Annotation>, Prefix> baseUnit = getBaseUnitClassAndPrefix(mirror);
            Class<? extends Annotation> baseUnitClass = baseUnit.first;

            // if the base unit isn't already added, add that first
            String baseUnitClassName = baseUnitClass.getCanonicalName().intern();
            if (!externalQualsMap.containsKey(baseUnitClassName)) {
                loadExternalUnit(baseUnitClassName);
            }

            // Build the alias's standard annotation
            AnnotationMirror result = buildBaseUnitAnnotationForAlias(mirror);
            // Get the name of the aliased annotation
            String aname = mirror.getAnnotationType().toString().intern();
            // add to the alias map
            aliasMap.put(aname, result);
        }

        // process the units annotation and add its corresponding units
        // relations class
        unitsRelations.addUnitsRelations(annoClass);
    }

    /**
     * Checks to see if anno is an alias annotation
     *
     * @param anno a units annotation
     * @return true if anno is an alias annotation
     */
    protected boolean isAliasedAnnotation(AnnotationMirror anno) {
        return getUnitsMultipleMetaAnnotation(anno) != null;
    }

    private /*@Nullable*/ AnnotationMirror getUnitsMultipleMetaAnnotation(AnnotationMirror anno) {
        for (AnnotationMirror metaAnno :
                anno.getAnnotationType().asElement().getAnnotationMirrors()) {
            // see if the meta annotation is UnitsMultiple
            if (isUnitsMultiple(metaAnno)) {
                return metaAnno;
            }
        }
        return null;
    }

    private boolean isUnitsMultiple(AnnotationMirror metaAnno) {
        return AnnotationUtils.areSameByClass(metaAnno, UnitsMultiple.class);
    }

    /**
     * If given an alias annotation, this method will return a pair consisting of the alias's base
     * unit annotation class and the alias's prefix. If given any other annotation, this method will
     * return null.
     *
     * @param anno a units annotation
     * @return the pair, or null
     */
    private /*@Nullable*/ Pair<Class<? extends Annotation>, Prefix> getBaseUnitClassAndPrefix(
            AnnotationMirror anno) {
        AnnotationMirror unitsMultipleAnno = getUnitsMultipleMetaAnnotation(anno);

        // see if the annotation is an alias
        if (unitsMultipleAnno != null) {
            // retrieve the Class of the base unit annotation
            Class<? extends Annotation> baseUnitAnnoClass =
                    AnnotationUtils.getElementValueClass(unitsMultipleAnno, "quantity", true)
                            .asSubclass(Annotation.class);

            // TODO: does every alias have to have a Prefix?
            // retrieve the Prefix of the alias unit
            Prefix prefix =
                    AnnotationUtils.getElementValueEnum(
                            unitsMultipleAnno, "prefix", Prefix.class, true);

            // return the Class and the Prefix as a pair
            return Pair.<Class<? extends Annotation>, Prefix>of(baseUnitAnnoClass, prefix);
        } else {
            return null;
        }
    }

    /**
     * Given an alias annotation, this method builds a base unit annotation with the alias's prefix,
     * adds the alias to the aliasMap, and then returns the base annotation.
     *
     * <p>Given any other annotation, this method returns null.
     *
     * <p>e.g. given @kg this will build @g with prefix {@link Prefix#kilo}
     *
     * @param anno an alias annotation
     * @return the base unit annotation with the alias's prefix, null otherwise
     */
    private /*@Nullable*/ AnnotationMirror buildBaseUnitAnnotationForAlias(AnnotationMirror anno) {
        // Obtain the base unit class and alias prefix
        Pair<Class<? extends Annotation>, Prefix> baseUnit = getBaseUnitClassAndPrefix(anno);
        AnnotationMirror result = null;

        if (baseUnit != null) {
            Class<? extends Annotation> baseUnitClass = baseUnit.first;
            Prefix prefix = baseUnit.second;

            // Try to build a base unit annotation with the prefix applied
            result =
                    UnitsRelationsTools.buildAnnoMirrorWithSpecificPrefix(
                            processingEnv, baseUnitClass, prefix);

            // see if we are able to build the base unit annotation with the
            // alias's prefix
            if (result != null) {
                // aliases shouldn't have Prefix.one, but if it does then clean
                // it
                // up here
                if (UnitsRelationsTools.getPrefix(result) == Prefix.one) {
                    result = removePrefix(result);
                }
            }
        }

        return result;
    }

    // Converts all metric-prefixed units' alias annotations (eg @kg) into base
    // unit annotations with prefix values (eg @g(Prefix.kilo))
    @Override
    public AnnotationMirror aliasedAnnotation(AnnotationMirror anno) {
        // if it isn't an alias annotation, immediately return the annotation
        if (!isAliasedAnnotation(anno)) {
            return anno;
        }

        // Get the name of the aliased annotation
        String aname = anno.getAnnotationType().toString().intern();

        // See if we already have a map from this aliased annotation to its
        // corresponding base unit annotation
        if (aliasMap.containsKey(aname)) {
            // if so return it
            return aliasMap.get(aname);
        }

        AnnotationMirror result = buildBaseUnitAnnotationForAlias(anno);

        if (result != null) {
            // Assert that this annotation is a prefix multiple of a
            // Unit that's in the supported type qualifiers list
            assert isSupportedQualifier(result);

            // add this to the alias map
            aliasMap.put(aname, result);

            return result;
        } else {
            return super.aliasedAnnotation(anno);
        }
    }

    protected AnnotationMirror removePrefix(AnnotationMirror anno) {
        if (anno != null) {
            return UnitsRelationsTools.removePrefix(elements, anno);
        } else {
            return anno;
        }
    }

    private void createUnitsRelationsManager() {
        if (unitsRelations == null) {
            unitsRelations = new UnitsRelationsManager((UnitsChecker) checker, this);
        }
    }

    protected UnitsRelationsManager getUnitsRelationsManager() {
        if (unitsRelations == null) {
            createUnitsRelationsManager();
        }
        return unitsRelations;
    }

    protected UnitsMathOperatorsRelations getUnitsMathOperatorsRelations() {
        return mathOpRelations;
    }

    /**
     * Returns the type mirror representing class clazz through the element utils
     *
     * @param clazz a class literal
     * @return type mirror representing class clazz
     */
    protected TypeMirror getTypeMirror(Class<?> clazz) {
        return elements.getTypeElement(clazz.getCanonicalName()).asType();
    }

    /**
     * Checks to see if the underlying type of the annotated type mirror is the same as the
     * classType
     *
     * @param atm an annotated type mirror
     * @param classType a type mirror representing a class
     * @return true if the underlying type of the annotated type mirror is the same as the
     *     classType, false otherwise
     */
    protected boolean isSameUnderlyingType(AnnotatedTypeMirror atm, TypeMirror classType) {
        return isSameUnderlyingType(atm.getUnderlyingType(), classType);
    }

    /**
     * Checks to see if two type mirrors are the same by using typeUtils.isSameType to compare
     * instead of TypeMirror.equals as this will check only the underlying type and ignores
     * declarations on the type mirror.
     *
     * <p>The check results are also cached in typeMirrorSameCache in the UnitsATF for speed
     *
     * @param typeMirror1 TypeMirror 1
     * @param typeMirror2 TypeMirror 2
     * @return true if the underlying types are the same, false otherwise
     */
    private boolean isSameUnderlyingType(TypeMirror typeMirror1, TypeMirror typeMirror2) {
        if (typeMirror1 == typeMirror2) {
            return true;
        }

        String lhtKey = typeMirror1.toString().intern();
        String rhtKey = typeMirror2.toString().intern();

        // if the cache has seen both the left and right types before,
        // return the cached value
        if (typeMirrorSameCache.containsKey(lhtKey)) {
            Map<String, Boolean> innerMap = typeMirrorSameCache.get(lhtKey);
            if (innerMap != null && innerMap.containsKey(rhtKey)) {
                return innerMap.get(rhtKey);
            }
        }

        // otherwise compute and cache the type mirrors

        // use typeUtils.isSameType instead of TypeMirror.equals as this
        // will check only the underlying type and ignores declarations on
        // the type mirror
        Boolean isSame = checker.getTypeUtils().isSameType(typeMirror1, typeMirror2);

        if (typeMirrorSameCache.containsKey(lhtKey)) {
            // add to existing inner map
            typeMirrorSameCache.get(lhtKey).put(rhtKey, isSame);
        } else {
            // add a new inner map with the right type as key, and add this
            // inner map to the outer map
            Map<String, Boolean> newInnerMap = new HashMap<>();
            newInnerMap.put(rhtKey, isSame);
            typeMirrorSameCache.put(lhtKey, newInnerMap);
        }

        return isSame;
    }

    // =========================================================
    // Tree Annotators
    // =========================================================

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new UnitsPropagationTreeAnnotator((UnitsChecker) checker, this),
                new ImplicitsTreeAnnotator(this));
    }

    // =========================================================
    // Qualifier Hierarchy
    // =========================================================

    /** Programatically set the Bottom qualifier as the bottom of the hierarchy. */
    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new UnitsQualifierHierarchy(factory, BOTTOM, this);
    }

    // =========================================================
    // Type Hierarchy
    // =========================================================

    // Override to allow covariant type arguments
    @Override
    protected TypeHierarchy createTypeHierarchy() {
        return new UnitsTypeHierarchy(checker, this);
    }
}
