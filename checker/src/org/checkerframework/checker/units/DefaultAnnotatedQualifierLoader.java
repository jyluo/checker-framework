package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.util.AnnotationBuilder;

public class DefaultAnnotatedQualifierLoader extends AnnotatedQualifierLoader {

    public DefaultAnnotatedQualifierLoader(ProcessingEnvironment pe, Class<? extends BaseTypeChecker> checker) {
        super(pe, checker);
    }

    @Override
    protected AnnotationMirror createAnnotationMirrorFromClass(Class<? extends Annotation> annoClass) {
        // build the annotation mirror
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, annoClass);
        AnnotationMirror annoMirroResult = builder.build();
        return annoMirroResult;
    }
}
