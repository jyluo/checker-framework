package org.checkerframework.checker.units;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.util.DefaultAnnotationFormatter;
import org.checkerframework.javacutil.AnnotationUtils;

// Define a custom formatter to ensure that any annotation which uses Prefix.one appear without this
// prefix in generated error messages. All other prefixes are kept in the visual presentation.
public final class UnitsAnnotationFormatter extends DefaultAnnotationFormatter {
    protected final Elements elements;

    public UnitsAnnotationFormatter(BaseTypeChecker checker) {
        this.elements = checker.getElementUtils();
    }

    @Override
    public String formatAnnotationString(
            Collection<? extends AnnotationMirror> annos, boolean printInvisible) {
        // create an empty annotation set
        Set<AnnotationMirror> trimmedAnnoSet = AnnotationUtils.createAnnotationSet();

        // remove Prefix.one in the set of annotation mirrors
        for (AnnotationMirror anno : annos) {
            if (UnitsRelationsTools.getPrefix(anno) == Prefix.one) {
                anno = UnitsRelationsTools.removePrefix(elements, anno);
            }
            // add to set
            trimmedAnnoSet.add(anno);
        }

        return super.formatAnnotationString(
                Collections.unmodifiableSet(trimmedAnnoSet), printInvisible);
    }
}
