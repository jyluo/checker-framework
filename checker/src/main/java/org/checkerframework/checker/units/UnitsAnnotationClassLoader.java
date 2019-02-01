package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.BaseUnit;
import org.checkerframework.checker.units.qual.UnitsAlias;
import org.checkerframework.checker.units.utils.UnitsRepresentationUtils;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotationClassLoader;

public class UnitsAnnotationClassLoader extends AnnotationClassLoader {

    /** reference to the units representation utilities library */
    protected final UnitsRepresentationUtils unitsRepUtils;

    protected final Map<String, Class<? extends Annotation>> externalUnitsMap = new HashMap<>();

    public UnitsAnnotationClassLoader(
            BaseTypeChecker checker, UnitsRepresentationUtils unitsRepUtils) {
        super(checker);
        this.unitsRepUtils = unitsRepUtils;
    }

    /**
     * Custom filter for units annotations:
     *
     * <p>This filter will ignore (by returning false) any units annotation which is an alias of
     * another base unit annotation. Alias annotations can still be used in source code; they are
     * converted into a base annotation by {@link
     * UnitsAnnotatedTypeFactory#aliasedAnnotation(AnnotationMirror)}. This filter simply makes sure
     * that the alias annotations themselves don't become part of the type hierarchy as their base
     * annotations already are in the hierarchy.
     */
    @Override
    protected boolean isSupportedAnnotationClass(Class<? extends Annotation> annoClass) {

        if (annoClass.getAnnotation(BaseUnit.class) != null) {
            unitsRepUtils.addBaseUnit(annoClass);
            return false;
        }

        if (annoClass.getAnnotation(UnitsAlias.class) != null) {
            unitsRepUtils.addAliasUnit(annoClass);
            return false;
        }

        // Not an alias unit
        return true;
    }

    public void loadAllExternalUnits(String qualNames, String qualDirectories) {
        // load external individually named units
        if (qualNames != null) {
            for (String qualName : qualNames.split(",")) {
                loadExternalUnit(qualName);
            }
        }

        // load external directories of units
        if (qualDirectories != null) {
            for (String directoryName : qualDirectories.split(":")) {
                loadExternalDirectory(directoryName);
            }
        }
    }

    /** Loads and processes a single external units qualifier. */
    private void loadExternalUnit(String annoName) {
        // loadExternalAnnotationClass() returns null for alias units
        Class<? extends Annotation> loadedClass = loadExternalAnnotationClass(annoName);
        if (loadedClass != null) {
            System.err.println(loadedClass);
            //            addUnitToExternalQualMap(loadedClass);
        }
    }

    /** Loads and processes the units qualifiers from a single external directory. */
    private void loadExternalDirectory(String directoryName) {
        Set<Class<? extends Annotation>> annoClassSet =
                loadExternalAnnotationClassesFromDirectory(directoryName);

        for (Class<? extends Annotation> loadedClass : annoClassSet) {
            System.err.println(loadedClass);
            //            addUnitToExternalQualMap(annoClass);
        }
    }

    public Collection<? extends Class<? extends Annotation>> getExternalUnits() {
        return externalUnitsMap.values();
    }
}
