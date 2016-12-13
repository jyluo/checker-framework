package qual;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.UnitsRelations;
import org.checkerframework.checker.units.UnitsRelationsTools;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.Scalar;
import org.checkerframework.checker.units.qual.time.duration.s;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
 */

/** Relations among units of frequency. */
public class FrequencyRelations implements UnitsRelations {
    protected AnnotationMirror hertz, kilohertz, second, millisecond, scalar;

    public UnitsRelations init(ProcessingEnvironment env) {
        // create Annotation Mirrors, each representing a Unit Annotation
        hertz = UnitsRelationsTools.buildAnnoMirrorWithDefaultPrefix(env, Hz.class);
        kilohertz =
                UnitsRelationsTools.buildAnnoMirrorWithSpecificPrefix(env, Hz.class, Prefix.kilo);
        second = UnitsRelationsTools.buildAnnoMirrorWithDefaultPrefix(env, s.class);
        millisecond =
                UnitsRelationsTools.buildAnnoMirrorWithSpecificPrefix(env, s.class, Prefix.milli);
        scalar = UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(env, Scalar.class);

        return this;
    }

    /** No multiplications yield Hertz. */
    public /* @Nullable */ AnnotationMirror multiplication(
            AnnotatedTypeMirror lht, AnnotatedTypeMirror rht) {
        // hertz * seconds => scalar
        // kilohertz * millisecond => scalar
        if (UnitsRelationsTools.hasSpecificUnit(lht, hertz)
                        && UnitsRelationsTools.hasSpecificUnit(rht, second)
                || UnitsRelationsTools.hasSpecificUnit(lht, second)
                        && UnitsRelationsTools.hasSpecificUnit(rht, hertz)
                || UnitsRelationsTools.hasSpecificUnit(lht, kilohertz)
                        && UnitsRelationsTools.hasSpecificUnit(rht, millisecond)
                || UnitsRelationsTools.hasSpecificUnit(lht, millisecond)
                        && UnitsRelationsTools.hasSpecificUnit(rht, kilohertz)) {
            return scalar;
        }

        // If the types of the parameters don't match these pairs, then return null so that other
        // Units Relations can be checked
        return null;
    }

    /**
     * Division of a scalar by seconds yields Hertz. Division of a scalar by milliseconds yields
     * Kilohertz. Other divisions yield an unannotated value.
     */
    public /* @Nullable */ AnnotationMirror division(
            AnnotatedTypeMirror lht, AnnotatedTypeMirror rht) {
        if (UnitsRelationsTools.hasNoUnits(lht)) {
            // scalar / millisecond => kilohertz
            if (UnitsRelationsTools.hasSpecificUnit(rht, millisecond)) {
                return kilohertz;
            }
            // scalar / second => hertz
            else if (UnitsRelationsTools.hasSpecificUnit(rht, second)) {
                return hertz;
            }
        }

        // If the types of the parameters don't match these pairs, then return null so that other
        // Units Relations can be checked
        return null;
    }
}
