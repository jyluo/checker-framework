package org.checkerframework.checker.units;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Unit;
import org.checkerframework.framework.util.AnnotationBuilder;

public class UnitsAnnoBuilder {

    public static AnnotationMirror createUnitsAnnotation(
            ProcessingEnvironment processingEnv, final String value) {

        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, Unit.class);
        // TODO: validation
        // TODO: normalization
        // if (groupCount > 0) {
        builder.setValue("value", value);
        // }
        return builder.build();
    }
}
