import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.tools.Diagnostic.Kind;

import org.checkerframework.checker.units.UnitsRelations;
import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.qual.Prefix;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
 */

/** Relations among units of frequency. */
public class FrequencyRelations implements UnitsRelations {

    protected AnnotationMirror hz, s;
    ProcessingEnvironment processingEnv;

    public UnitsRelations init(ProcessingEnvironment env) {
        processingEnv = env;

        AnnotationBuilder builder = new AnnotationBuilder(env, Hz.class);
        builder.setValue("value", Prefix.one);
        hz = builder.build();

        builder = new AnnotationBuilder(env, s.class);
        builder.setValue("value", Prefix.one);
        s = builder.build();

        return this;
    }

    // No multiplications yield Hertz.
    public /*@Nullable*/ AnnotationMirror multiplication(AnnotatedTypeMirror p1, AnnotatedTypeMirror p2) {
        return null;
    }

    // Division of a scalar by seconds yields Hertz.
    // Other divisions yield an unannotated value.
    public /*@Nullable*/ AnnotationMirror division(AnnotatedTypeMirror p1, AnnotatedTypeMirror p2) {

        // AnnotationUtils.containsSameIgnoringValues(p2.getAnnotations(), s)
        if (noUnits(p1) && AnnotationUtils.containsSame(p2.getAnnotations(), s)) {
            return hz;
        }

        return null;
    }

    // returns true if the type mirror either has no annotations or has only 1 annotation and it is UnknownUnits
    private boolean noUnits(AnnotatedTypeMirror t) {
        Set<AnnotationMirror> annos = t.getAnnotations();
        return annos.isEmpty() ||
                (annos.size() == 1 &&
                AnnotationUtils.areSameByClass(annos.iterator().next(), UnknownUnits.class));
    }

}
