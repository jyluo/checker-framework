package org.checkerframework.checker.experimental.units_qual_poly;

import org.checkerframework.qualframework.base.Checker;
import org.checkerframework.qualframework.poly.QualifierParameterChecker;
import org.checkerframework.qualframework.poly.format.SurfaceSyntaxFormatterConfiguration;
import org.checkerframework.qualframework.poly.format.SurfaceSyntaxQualParamsFormatter.AnnotationParts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link Checker} for the Units-Qual-Param type system.
 */
public class UnitsQualPolyChecker extends QualifierParameterChecker<Units> {

    @Override
    protected UnitsQualifiedTypeFactory createTypeFactory() {
        return new UnitsQualifiedTypeFactory(this);
    }

    protected Set<?> getInvisibleQualifiers() {
        return new HashSet<>(
                Arrays.asList(
                        this.getTypeFactory().getQualifierHierarchy().getBottom(),
                        this.getTypeFactory().getQualifierHierarchy().getTop(),
                        Units.BOTTOM, Units.UnitsUnknown));
    }

    @Override
    protected SurfaceSyntaxFormatterConfiguration<Units> createSurfaceSyntaxFormatterConfiguration() {
        return new UnitsSurfaceSyntaxConfiguration();
    }

    private class UnitsSurfaceSyntaxConfiguration extends SurfaceSyntaxFormatterConfiguration<Units> {

        // stores the set of annotation names for which we won't print out the annotation
        private final Set<String> SUPPRESS_NAMES = new HashSet<>(
                Arrays.asList("UnknownUnits", "UnitsBottom", "UnitsMultiple"));
        //TODO: change this to soft-coded annotation names
        //TODO: remove unitsmultiple?

        public UnitsSurfaceSyntaxConfiguration() {
            super(Units.UnitsUnknown, Units.BOTTOM,
                    UnitsQualPolyChecker.this.getContext().getTypeFactory().getQualifierHierarchy().getTop(),
                    UnitsQualPolyChecker.this.getContext().getTypeFactory().getQualifierHierarchy().getBottom());
        }

        // decides where to print annotations or not
        @Override
        protected boolean shouldPrintAnnotation(AnnotationParts anno, boolean printInvisibleQualifiers) {
            return printInvisibleQualifiers || !(SUPPRESS_NAMES.contains(anno.getName()));
        }

        // generates annotation printouts for qualifiers that don't actually exist as an annotation
        // using AnnotationBuilder to create the annotation would have required an actual annotation to begin with
        @Override
        protected AnnotationParts getTargetTypeSystemAnnotation(Units qual) {

            // construct an annotation for a normal regex value
            if (qual instanceof Units) {
                AnnotationParts anno = new AnnotationParts("Units");
                // TODO: change to prefix
                anno.put("value", String.valueOf(qual.getPrefix()));
                // TODO: type conversion of Regex to RegexVal???
                //anno.put("value", String.valueOf(((UnitsVal) qual).getCount()));
                return anno;

                // construct an annotation for partial regex values
                /*
                } else if (qual instanceof PartialUnits) {
                    AnnotationParts anno = new AnnotationParts("PartialUnits");
                    anno.putQuoted("value", ((PartialUnits) qual).getPartialValue());
                    return anno;
                 */
            } else {
                // the annotation is the same as the name of the qualifier
                return new AnnotationParts(qual.toString());
            }
        }
    }

}
