package org.checkerframework.checker.units;

import java.lang.annotation.Annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.UnitsMultiple;
import org.checkerframework.framework.util.AnnotationBuilder;

public class UnitsAnnotatedQualifierLoader extends AnnotatedQualifierLoader {

    public UnitsAnnotatedQualifierLoader(ProcessingEnvironment pe) {
        super(pe, UnitsChecker.class);
    }

    @Override
    protected AnnotationMirror createAnnotationMirrorFromClass(Class<? extends Annotation> annoClass) {
        // build the initial annotation mirror (missing prefix)
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, annoClass);
        AnnotationMirror initialResult = builder.build();

        // further refine to see if the annotation is an alias of some other SI Unit annotation
        for(AnnotationMirror metaAnno : initialResult.getAnnotationType().asElement().getAnnotationMirrors() ) {
            // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Meta Anno: " + metaAnno);

            // TODO : special treatment of invisible qualifiers?

            // annotations which are a SI prefix multiple of some base unit 
            if( metaAnno.getAnnotationType().toString().equals(UnitsMultiple.class.getCanonicalName())) {
                // classic Units checker does not need the annotations for SI prefix multiples
                return null;
            }
        }

        // Not an alias unit
        return initialResult;
    }

}
