package org.checkerframework.checker.units.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.units.UnitsAnnotatedTypeFactory;
import org.checkerframework.checker.units.qual.BUC;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.PolyUnit;
import org.checkerframework.checker.units.qual.UnitsAlias;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnitsRep;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Utility class containing logic for creating and converting internal representations of units
 * between its 3 primary forms: {@link UnitsRep} as annotation mirrors and {@link TypecheckUnit}.
 *
 * <p>TODO: {@code @Unit}, and alias forms.
 */
public class UnitsRepresentationUtils {
    protected ProcessingEnvironment processingEnv;
    protected Elements elements;

    /** An instance of {@link PolyAll} as an {@link AnnotationMirror} */
    public AnnotationMirror POLYALL;

    /** An instance of {@link PolyUnit} as an {@link AnnotationMirror} */
    public AnnotationMirror POLYUNIT;

    /** An instance of {@link UnitsRep} with no values in its elements */
    public AnnotationMirror RAWUNITSREP;

    /** Instances of {@link UnitsRep} with values to represent UnknownUnits and UnitsBottom */
    public AnnotationMirror TOP;

    public AnnotationMirror BOTTOM;

    /**
     * An instance of {@link UnitsRep} with default values in its elements, which represents
     * dimensionless
     */
    public AnnotationMirror DIMENSIONLESS;

    /** Instances of {@link UnknownUnits} and {@link UnitsBottom} for insertion to source; */
    public AnnotationMirror SURFACE_TOP;

    public AnnotationMirror SURFACE_BOTTOM;

    /**
     * Instances of time units for use with various time APIs, used by {@link
     * UnitsAnnotatedTypeFactory#UnitsImplicitsTreeAnnotator}
     */
    public AnnotationMirror SECOND, MILLISECOND, MICROSECOND, NANOSECOND;

    private final Map<AnnotationMirror, AnnotationMirror> fillMissingBaseUnitsCache =
            new HashMap<>();

    // A 1 to 1 mapping between an annotation mirror and its unique typecheck unit.
    private final Map<AnnotationMirror, TypecheckUnit> unitsRepAnnoCache = new HashMap<>();

    // Comparator used to sort annotation classes by their simple class name
    private static Comparator<Class<? extends Annotation>> annoClassComparator =
            new Comparator<Class<? extends Annotation>>() {
                @Override
                public int compare(Class<? extends Annotation> a1, Class<? extends Annotation> a2) {
                    return a1.getSimpleName().compareTo(a2.getSimpleName());
                }
            };

    /** The set of base units */
    private final Map<String, Class<? extends Annotation>> baseUnits = new TreeMap<>();

    /** All base units provided by the checker or user */
    private Set<String> baseUnitNames;

    /** The set of alias units */
    private final Set<Class<? extends Annotation>> aliasUnits = createSortedBaseUnitSet();

    /** A map from surface units annotation mirrors to their {@link UnitsRep}s representation. */
    private final Map<AnnotationMirror, AnnotationMirror> unitsAnnotationMirrorMap =
            AnnotationUtils.createAnnotationMap();

    /**
     * A set of the surface units annotation classes added to the {@link #unitsAnnotationMirrorMap}.
     */
    private final Set<Class<? extends Annotation>> surfaceUnitsSet = new HashSet<>();

    public UnitsRepresentationUtils(ProcessingEnvironment processingEnv, Elements elements) {
        this.processingEnv = processingEnv;
        this.elements = elements;
    }

    /** Creates {@link AnnotationMirror}s for all of the annotations in this class */
    public void postInit() {
        POLYALL = AnnotationBuilder.fromClass(elements, PolyAll.class);
        POLYUNIT = AnnotationBuilder.fromClass(elements, PolyUnit.class);

        RAWUNITSREP = AnnotationBuilder.fromClass(elements, UnitsRep.class);

        Map<String, Integer> zeroBaseDimensions = createZeroFilledBaseUnitsMap();
        TOP = createUnitsRepAnno(true, false, 0, zeroBaseDimensions);
        BOTTOM = createUnitsRepAnno(false, true, 0, zeroBaseDimensions);
        DIMENSIONLESS = createUnitsRepAnno(false, false, 0, zeroBaseDimensions);

        // Map<String, Integer> meterDimensions = createZeroFilledBaseUnitsMap();
        // meterDimensions.put("m", 1);
        // METER = createInternalUnit("Meter", false, false, 0, meterDimensions);

        unitsAnnotationMirrorMap.put(
                AnnotationBuilder.fromClass(elements, UnknownUnits.class), TOP);
        unitsAnnotationMirrorMap.put(
                AnnotationBuilder.fromClass(elements, UnitsBottom.class), BOTTOM);
        unitsAnnotationMirrorMap.put(
                AnnotationBuilder.fromClass(elements, Dimensionless.class), DIMENSIONLESS);

        surfaceUnitsSet.add(UnknownUnits.class);
        surfaceUnitsSet.add(UnitsBottom.class);
        surfaceUnitsSet.add(Dimensionless.class);

        for (Class<? extends Annotation> baseUnit : baseUnits.values()) {
            createInternalBaseUnit(baseUnit);
        }
        surfaceUnitsSet.addAll(baseUnits.values());

        for (Class<? extends Annotation> aliasUnit : aliasUnits) {
            createInternalAliasUnit(aliasUnit);
        }
        surfaceUnitsSet.addAll(aliasUnits);

        SURFACE_TOP = AnnotationBuilder.fromClass(elements, UnknownUnits.class);
        SURFACE_BOTTOM = AnnotationBuilder.fromClass(elements, UnitsBottom.class);

        Map<String, Integer> secondBaseMap = createZeroFilledBaseUnitsMap();
        secondBaseMap.put("s", 1);
        SECOND = createUnitsRepAnno(false, false, 0, secondBaseMap);
        MILLISECOND = createUnitsRepAnno(false, false, -3, secondBaseMap);
        MICROSECOND = createUnitsRepAnno(false, false, -6, secondBaseMap);
        NANOSECOND = createUnitsRepAnno(false, false, -9, secondBaseMap);

        // for (Entry<AnnotationMirror, AnnotationMirror> entry : unitsAnnotationMirrorMap
        // .entrySet()) {
        // System.err.println(" == built map " + entry.getKey() + " --> " + entry.getValue());
        // }
    }

    public static Set<Class<? extends Annotation>> createSortedBaseUnitSet() {
        return new TreeSet<>(annoClassComparator);
    }

    public static <TVal> Map<String, TVal> createSortedBaseUnitMap() {
        return new TreeMap<>();
    }

    public void addBaseUnit(Class<? extends Annotation> baseUnit) {
        baseUnits.put(baseUnit.getSimpleName(), baseUnit);
    }

    // public Class<? extends Annotation> getBaseUnitClass(String simpleClassName) {
    // return baseUnits.get(simpleClassName);
    // }

    public Set<String> baseUnits() {
        if (baseUnitNames == null) {
            baseUnitNames = Collections.unmodifiableSet(baseUnits.keySet());
        }
        return baseUnitNames;
    }

    public Set<Class<? extends Annotation>> surfaceUnitsSet() {
        return surfaceUnitsSet;
    }

    public void addAliasUnit(Class<? extends Annotation> aliasUnit) {
        aliasUnits.add(aliasUnit);
    }

    /**
     * Creates and returns an exponent values map with all defined base units sorted alphabetically
     * according to unit symbol name, and with the exponent values set to 0.
     */
    protected Map<String, Integer> createZeroFilledBaseUnitsMap() {
        Map<String, Integer> map = createSortedBaseUnitMap();
        for (String baseUnit : baseUnits()) {
            map.put(baseUnit, 0);
        }
        return map;
    }

    /**
     * Creates an {@link UnitsRep} representation for the given base unit and adds it to the alias
     * map.
     */
    private void createInternalBaseUnit(Class<? extends Annotation> baseUnitClass) {
        // check to see if the annotation has already been mapped before
        AnnotationMirror baseUnitAM = AnnotationBuilder.fromClass(elements, baseUnitClass);
        if (unitsAnnotationMirrorMap.containsKey(baseUnitAM)) {
            return;
        }

        Map<String, Integer> exponents = createZeroFilledBaseUnitsMap();

        // set the exponent of the given base unit to 1
        exponents.put(baseUnitClass.getSimpleName(), 1);
        // create the {@link UnitsRep} and add to alias map
        unitsAnnotationMirrorMap.put(baseUnitAM, createUnitsRepAnno(false, false, 0, exponents));
    }

    /**
     * Creates an {@link UnitsRep} representation for the given alias unit and adds it to the alias
     * map.
     */
    private void createInternalAliasUnit(Class<? extends Annotation> aliasUnitClass) {
        // check to see if the annotation has already been mapped before
        AnnotationMirror aliasUnitAM = AnnotationBuilder.fromClass(elements, aliasUnitClass);
        if (unitsAnnotationMirrorMap.containsKey(aliasUnitAM)) {
            return;
        }

        Map<String, Integer> exponents = createZeroFilledBaseUnitsMap();

        // replace default base unit exponents from anno, and accumulate prefixes
        UnitsAlias aliasInfo = aliasUnitClass.getAnnotation(UnitsAlias.class);
        for (BUC baseUnitComponent : aliasInfo.baseUnitComponents()) {
            exponents.put(baseUnitComponent.unit(), baseUnitComponent.exponent());
        }

        int prefix = aliasInfo.prefixExponent();

        unitsAnnotationMirrorMap.put(
                aliasUnitAM, createUnitsRepAnno(false, false, prefix, exponents));
    }

    /**
     * Returns the {@link UnitsRep} representation for the given surface annotation if it has been
     * created, null otherwise.
     *
     * @param anno an {@link AnnotationMirror} of an annotation
     * @return the internal representation unit as an {@link AnnotationMirror}
     */
    public AnnotationMirror getInternalAliasUnit(AnnotationMirror anno) {
        // check to see if the annotation has already been mapped before
        if (unitsAnnotationMirrorMap.containsKey(anno)) {
            return unitsAnnotationMirrorMap.get(anno);
        }

        return null;
    }

    /**
     * Returns the surface unit representation for the given {@link UnitsRep} annotation if
     * available, otherwise returns the given annotation unchanged.
     *
     * @param anno an {@link AnnotationMirror} of a {@link UnitsRep} annotation
     * @return the surface representation unit if available, otherwise the @UnitsRep annotation
     *     unchanged
     */
    public AnnotationMirror getSurfaceUnit(AnnotationMirror anno) {
        for (Entry<AnnotationMirror, AnnotationMirror> pair : unitsAnnotationMirrorMap.entrySet()) {
            if (AnnotationUtils.areSame(pair.getValue(), anno)) {
                return pair.getKey();
            }
        }

        return anno;
    }

    /*
     * It is a units annotation if we have built an alias for it in the past (this includes @m
     * --> @UnitsRep(..)), or is supported by the qual hierarchy, or it is a @UnitsRep
     * annotation (with possibly not all base units).
     */
    public boolean isUnitsAnnotation(
            BaseAnnotatedTypeFactory realTypeFactory, AnnotationMirror anno) {
        return unitsAnnotationMirrorMap.keySet().contains(anno)
                || realTypeFactory.isSupportedQualifier(anno)
                || AnnotationUtils.areSameByClass(anno, UnitsRep.class);
    }

    public boolean hasUnitsAnnotation(
            BaseAnnotatedTypeFactory realTypeFactory,
            Iterable<? extends AnnotationMirror> annotations) {
        for (AnnotationMirror anno : annotations) {
            if (isUnitsAnnotation(realTypeFactory, anno)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllBaseUnits(AnnotationMirror anno) {
        if (!AnnotationUtils.areSameByClass(anno, UnitsRep.class)) {
            return false;
        }

        // add declared base units from the anno to the map, filtering out duplicate base units
        Map<String, Integer> baseUnitsFromAnno = new HashMap<>();
        for (AnnotationMirror buAnno :
                AnnotationUtils.getElementValueArray(
                        anno, "baseUnitComponents", AnnotationMirror.class, true)) {
            String baseUnit = AnnotationUtils.getElementValue(buAnno, "unit", String.class, false);
            int exponent =
                    AnnotationUtils.getElementValue(buAnno, "exponent", Integer.class, false);
            // ensure the declared base unit is actually a supported base unit
            if (!baseUnits().contains(baseUnit)) {
                return false;
            }
            baseUnitsFromAnno.put(baseUnit, exponent);
        }

        // see if it has all of the base unit annotations
        return baseUnitsFromAnno.size() == baseUnits().size();
    }

    /**
     * Builds a fresh AnnotationMirror for the given UnitsRep annotation with any missing base units
     * filled in. For all other annotations, this returns null.
     */
    public AnnotationMirror fillMissingBaseUnits(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, UnitsRep.class)) {
            if (fillMissingBaseUnitsCache.containsKey(anno)) {
                return fillMissingBaseUnitsCache.get(anno);
            }

            TypecheckUnit unit = createTypecheckUnit(anno);
            AnnotationMirror filledInAM = createInternalUnit(unit);

            fillMissingBaseUnitsCache.put(anno, filledInAM);

            return filledInAM;
        } else {
            // not an {@link UnitsRep}s annotation
            return null;
        }
    }

    public TypecheckUnit createTypecheckUnit(AnnotationMirror anno) {
        if (unitsRepAnnoCache.containsKey(anno)) {
            return unitsRepAnnoCache.get(anno);
        }

        TypecheckUnit unit = new TypecheckUnit(this);

        // if it is a polyunit annotation, generate top
        if (AnnotationUtils.areSameByClass(anno, PolyUnit.class)
                || AnnotationUtils.areSameByClass(anno, PolyAll.class)) {
            unit.setUnknownUnits(true);
        }
        // if it is a units internal annotation, generate the {@link UnitsRep}
        else if (AnnotationUtils.areSameByClass(anno, UnitsRep.class)) {
            unit.setUnknownUnits(AnnotationUtils.getElementValue(anno, "top", Boolean.class, true));
            unit.setUnitsBottom(AnnotationUtils.getElementValue(anno, "bot", Boolean.class, true));
            unit.setPrefixExponent(
                    AnnotationUtils.getElementValue(anno, "prefixExponent", Integer.class, true));

            Map<String, Integer> exponents = createSortedBaseUnitMap();
            // default all base units to exponent 0
            for (String bu : baseUnits()) {
                exponents.put(bu, 0);
            }
            // replace base units with values in annotation
            for (AnnotationMirror bu :
                    AnnotationUtils.getElementValueArray(
                            anno, "baseUnitComponents", AnnotationMirror.class, true)) {
                exponents.put(
                        AnnotationUtils.getElementValue(bu, "unit", String.class, false),
                        AnnotationUtils.getElementValue(bu, "exponent", Integer.class, false));
            }

            for (String bu : exponents.keySet()) {
                unit.setExponent(bu, exponents.get(bu));
            }
        } else {
            // not a units annotation
            return null;
        }
        unitsRepAnnoCache.put(anno, unit);

        return unit;
    }

    public AnnotationMirror createInternalUnit(TypecheckUnit unit) {
        // see if cache already has a mapping, if so return from cache
        for (Entry<AnnotationMirror, TypecheckUnit> entry : unitsRepAnnoCache.entrySet()) {
            if (unit.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        // otherwise create an {@link UnitsRep} for the typecheck unit and add to cache
        AnnotationMirror anno =
                createUnitsRepAnno(
                        unit.isUnknownUnits(),
                        unit.isUnitsBottom(),
                        unit.getPrefixExponent(),
                        unit.getExponents());

        unitsRepAnnoCache.put(anno, unit);
        return anno;
    }

    public AnnotationMirror createUnitsRepAnno(
            boolean top, boolean bot, int prefixExponent, Map<String, Integer> exponents) {
        // not allowed to set both a UU and UB to true on the same annotation
        assert !(top && bot);

        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, UnitsRep.class);

        List<AnnotationMirror> expos = new ArrayList<>();
        for (String key : exponents.keySet()) {
            // Construct BaseUnit annotations for each exponent
            AnnotationBuilder bucBuilder = new AnnotationBuilder(processingEnv, BUC.class);
            bucBuilder.setValue("unit", key);
            bucBuilder.setValue("exponent", exponents.get(key));
            expos.add(bucBuilder.build());
        }

        // See {@link UnitsRep}
        builder.setValue("top", top);
        builder.setValue("bot", bot);
        builder.setValue("prefixExponent", prefixExponent);
        builder.setValue("baseUnitComponents", expos);
        return builder.build();
    }
}
