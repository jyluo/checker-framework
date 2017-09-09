package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.BaseUnit;
import org.checkerframework.checker.units.qual.UnitAlias;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotationClassLoader;

public class UnitsAnnotationClassLoader extends AnnotationClassLoader {

    public UnitsAnnotationClassLoader(BaseTypeChecker checker) {
        super(checker);
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
        // Filter out any annotation that has a @UnitAlias or @BaseUnit meta-annotation.
        return (annoClass.getAnnotation(UnitAlias.class) == null
                && annoClass.getAnnotation(BaseUnit.class) == null);
    }
}
