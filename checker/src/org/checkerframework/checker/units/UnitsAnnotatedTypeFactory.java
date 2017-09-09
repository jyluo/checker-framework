package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.checker.units.parser.UnitsParser;
import org.checkerframework.checker.units.parser.UnitsTokenizer;
import org.checkerframework.checker.units.parser.ast.UnitsASTNode;
import org.checkerframework.checker.units.qual.BaseUnit;
import org.checkerframework.checker.units.qual.UnitAlias;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

/** Annotated type factory for the Units Checker. */
public class UnitsAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    protected final AnnotationMirror unknownUnits =
            AnnotationBuilder.fromClass(elements, UnknownUnits.class);
    protected final AnnotationMirror unitsBottom =
            AnnotationBuilder.fromClass(elements, UnitsBottom.class);

    public UnitsAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);
        this.postInit();

        // TODO: REMOVE
        // Temp test spot
        UnitsTokenizer tokenizer = new UnitsTokenizer();
        UnitsParser parser = new UnitsParser();

        try {
            List<?> tokens = tokenizer.scan("((a / b))");
            processingEnv.getMessager().printMessage(Kind.NOTE, " tokens: " + tokens.toString());

            tokens = tokenizer.scan("m/s");
            assert tokens.size() == 3;
            processingEnv.getMessager().printMessage(Kind.NOTE, " tokens: " + tokens.toString());

            UnitsASTNode tree = parser.parse("s^-2^2", processingEnv.getMessager());
            processingEnv.getMessager().printMessage(Kind.NOTE, " tree: " + tree.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        // Use the Units Annotated Type Loader instead of the default one
        loader = new UnitsAnnotationClassLoader(checker);

        // get all the loaded annotations
        Set<Class<? extends Annotation>> qualSet = new HashSet<Class<? extends Annotation>>();
        qualSet.addAll(getBundledTypeQualifiersWithPolyAll());

        // TODO: load all the external units
        // loadAllExternalUnits();

        // copy all loaded external Units to qual set
        // qualSet.addAll(externalQualsMap.values());

        for (Class<? extends Annotation> qual : qualSet) {
            processingEnv.getMessager().printMessage(Kind.NOTE, "loaded qual: " + qual.toString());
        }

        return qualSet;
    }

    @Override
    public AnnotationMirror aliasedAnnotation(AnnotationMirror annotation) {
        // processingEnv.getMessager().printMessage(Kind.NOTE, "aliasing qual: " +
        // annotation.toString());

        for (AnnotationMirror metaAnnotation :
                annotation.getAnnotationType().asElement().getAnnotationMirrors()) {

            // If it is an alias unit, construct the corresponding @Unit unit.
            if (AnnotationUtils.areSameByClass(metaAnnotation, UnitAlias.class)) {
                String value =
                        AnnotationUtils.getElementValue(
                                metaAnnotation, "value", String.class, false);
                return UnitsAnnoBuilder.createUnitsAnnotation(processingEnv, value);
            } else if (AnnotationUtils.areSameByClass(metaAnnotation, BaseUnit.class)) {
                String value = AnnotationUtils.annotationSimpleName(annotation);
                return UnitsAnnoBuilder.createUnitsAnnotation(processingEnv, value);
            }
        }

        // TODO: ensure every @Unit with no values take on default value of "1"

        return super.aliasedAnnotation(annotation);
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        // Don't call super.createTreeAnnotator because it includes
        // PropagationTreeAnnotator which
        // is incorrect.
        return new ListTreeAnnotator(
                new UnitsPropagationTreeAnnotator(this), new ImplicitsTreeAnnotator(this));
    }

    /** A class for adding annotations based on tree */
    private class UnitsPropagationTreeAnnotator extends PropagationTreeAnnotator {

        UnitsPropagationTreeAnnotator(UnitsAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
            AnnotatedTypeMirror lht = getAnnotatedType(node.getLeftOperand());
            AnnotatedTypeMirror rht = getAnnotatedType(node.getRightOperand());
            Tree.Kind kind = node.getKind();

            switch (kind) {
                case MINUS:
                case PLUS:
                    if (lht.getAnnotations().equals(rht.getAnnotations())) {
                        // Has the same units for both operands.
                        type.replaceAnnotations(lht.getAnnotations());
                    } else {
                        // otherwise it results in unknownUnits
                        type.replaceAnnotation(unknownUnits);
                    }
                    break;
                case DIVIDE:
                    // TODO: replace by generating a unit as the difference of the operands
                    // UnitsAnnoBuilder.createUnitsAnnotation(processingEnv, "?");
                    type.replaceAnnotation(unknownUnits);
                    break;
                case MULTIPLY:
                    // TODO: replace by generating a unit as the sum of the operands
                    type.replaceAnnotation(unknownUnits);
                    break;
                case REMAINDER:
                    // in modulo operation, it always returns the left unit regardless of what
                    // it is (unknown, or some unit)
                    type.replaceAnnotations(lht.getAnnotations());
                    break;
                default:
                    // Placeholders for unhandled binary operations
                    // Do nothing
            }

            return null;
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node, AnnotatedTypeMirror type) {
            ExpressionTree var = node.getVariable();
            AnnotatedTypeMirror varType = getAnnotatedType(var);

            type.replaceAnnotations(varType.getAnnotations());
            return null;
        }
    }

    /** Set the Bottom qualifier as the bottom of the hierarchy. */
    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new UnitsQualifierHierarchy(factory);
    }

    protected class UnitsQualifierHierarchy extends MultiGraphQualifierHierarchy {

        public UnitsQualifierHierarchy(MultiGraphFactory mgf) {
            super(mgf);
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            // processingEnv.getMessager().printMessage(Kind.NOTE, "sub: " +
            // subAnno.toString());
            // processingEnv.getMessager().printMessage(Kind.NOTE, "sup: " +
            // superAnno.toString());

            if (AnnotationUtils.areSame(superAnno, unknownUnits)) {
                return true;
            } else if (AnnotationUtils.areSame(subAnno, unitsBottom)) {
                return true;
            } else {
                return AnnotationUtils.areSame(subAnno, superAnno);
            }

            // There's no need for super check if we know the annotations are always @Unit
            // TODO: gracefully detect and deal with this, to ensure all annos are in
            // hierarchy.
            // return super.isSubtype(subAnno, superAnno);
        }

        // Overriding leastUpperBound due to the fact that alias annotations are
        // not placed in the Supported Type Qualifiers set, instead, their base
        // SI units are in the set.
        // Whenever an alias annotation or prefix-multiple of a base SI unit is
        // used in ternary statements or through mismatched PolyUnit method
        // parameters, we handle the LUB resolution here so that these units can
        // correctly resolve to an LUB Unit.
        @Override
        public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {
            return super.leastUpperBound(a1, a2);
        }
    }
}
