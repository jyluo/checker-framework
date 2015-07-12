package org.checkerframework.checker.experimental.units_qual_poly;

import org.checkerframework.checker.experimental.Units_qual.Units;
import org.checkerframework.checker.experimental.Units_qual.Units.PartialUnits;
import org.checkerframework.checker.experimental.Units_qual.Units.UnitsVal;
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
                        Units.BOTTOM, Units.TOP));
    }

    @Override
    protected SurfaceSyntaxFormatterConfiguration<Units> createSurfaceSyntaxFormatterConfiguration() {
        return new UnitsSurfaceSyntaxConfiguration();
    }

    private class UnitsSurfaceSyntaxConfiguration extends SurfaceSyntaxFormatterConfiguration<Units> {

        private final Set<String> SUPPRESS_NAMES = new HashSet<>(
                Arrays.asList("UnitsTop", "UnitsBot", "PartialUnits"));

        public UnitsSurfaceSyntaxConfiguration() {
            super(Units.TOP, Units.BOTTOM,
                    UnitsQualPolyChecker.this.getContext().getTypeFactory().getQualifierHierarchy().getTop(),
                    UnitsQualPolyChecker.this.getContext().getTypeFactory().getQualifierHierarchy().getBottom());
        }

        @Override
        protected boolean shouldPrintAnnotation(AnnotationParts anno, boolean printInvisibleQualifiers) {
            return printInvisibleQualifiers || !(SUPPRESS_NAMES.contains(anno.getName()));
        }

        @Override
        protected AnnotationParts getTargetTypeSystemAnnotation(Units qual) {

            if (qual instanceof UnitsVal) {
                AnnotationParts anno = new AnnotationParts("Units");
                anno.put("value", String.valueOf(((UnitsVal) qual).getCount()));
                return anno;

            } else if (qual instanceof PartialUnits) {
                AnnotationParts anno = new AnnotationParts("PartialUnits");
                anno.putQuoted("value", ((PartialUnits) qual).getPartialValue());
                return anno;

            } else {
                return new AnnotationParts(qual.toString());
            }
        }
    }

}
