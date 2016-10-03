package org.checkerframework.checker.zulu;

import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.zulu.qual.ZuluBOTTOM;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;

/*
 * >>> import org.checkerframework.checker.nullness.qual.Nullable;
 */

/**
 * Annotated type factory for the Units Checker.
 *
 * Handles multiple names for the same unit, with different prefixes, e.g. @kg is the same
 * as @g(Prefix.kilo).
 *
 * Supports relations between units, e.g. if "m" is a variable of type "@m" and "s" is a variable of
 * type "@s", the division "m/s" is automatically annotated as "mPERs", the correct unit for the
 * result.
 */
public class ZuluAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    protected final AnnotationMirror BOTTOM =
            AnnotationUtils.fromClass(processingEnv.getElementUtils(), ZuluBOTTOM.class);

    public ZuluAnnotatedTypeFactory(BaseTypeChecker checker) {
        // use flow inference
        super(checker, true);

        this.postInit();
    }

    // =========================================================
    // Tree Annotators
    // =========================================================

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new PropagationTreeAnnotator(this), new ImplicitsTreeAnnotator(this));
    }

    // =========================================================
    // Qualifier Hierarchy
    // =========================================================

    /**
     * Set the Bottom qualifier as the bottom of the hierarchy.
     */
    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new GraphQualifierHierarchy(factory, BOTTOM);
    }
}
