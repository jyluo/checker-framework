package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UnaryTree;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.checker.units.utils.UnitsRepresentationUtils;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Units visitor.
 *
 * <p>Ensure consistent use of compound assignments.
 */
public class UnitsVisitor extends BaseTypeVisitor<UnitsAnnotatedTypeFactory> {
    /** reference to the units representation utilities library */
    protected UnitsRepresentationUtils unitsRepUtils;

    public UnitsVisitor(BaseTypeChecker checker) {
        super(checker);
        unitsRepUtils = atypeFactory.getUnitsRepresentationUtils();
    }

    @Override
    public Void visitUnary(UnaryTree node, Void p) {
        /** Unary increment and decrement is always type safe */
        if ((node.getKind() == Kind.PREFIX_DECREMENT)
                || (node.getKind() == Kind.PREFIX_INCREMENT)
                || (node.getKind() == Kind.POSTFIX_DECREMENT)
                || (node.getKind() == Kind.POSTFIX_INCREMENT)) {
            return null;
        } else {
            return super.visitUnary(node, p);
        }
    }

    @SuppressWarnings("fallthrough")
    @Override
    public Void visitBinary(BinaryTree binaryTree, Void p) {
        AnnotatedTypeMirror lhsATM = atypeFactory.getAnnotatedType(binaryTree.getLeftOperand());
        AnnotatedTypeMirror rhsATM = atypeFactory.getAnnotatedType(binaryTree.getRightOperand());
        AnnotationMirror lhsAM = lhsATM.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);
        AnnotationMirror rhsAM = rhsATM.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);

        switch (binaryTree.getKind()) {
            case PLUS:
                // if it is not a string concatenation and the units don't match, issue warning
                // if (!TreeUtils.isStringConcatenation(binaryTree)
                // && !AnnotationUtils.areSame(lhsAM, rhsAM)) {
                // checker.report(Result.failure("addition.unit.mismatch",
                // atypeFactory.getAnnotationFormatter().formatAnnotationMirror(lhsAM),
                // atypeFactory.getAnnotationFormatter().formatAnnotationMirror(rhsAM)),
                // binaryTree);
                // }
                break;
            case MINUS:
                // if (!AnnotationUtils.areSame(lhsAM, rhsAM)) {
                // checker.report(Result.failure("subtraction.unit.mismatch",
                // atypeFactory.getAnnotationFormatter().formatAnnotationMirror(lhsAM),
                // atypeFactory.getAnnotationFormatter().formatAnnotationMirror(rhsAM)),
                // binaryTree);
                // }
                break;
            case EQUAL_TO: // ==
            case NOT_EQUAL_TO: // !=
            case GREATER_THAN: // >
            case GREATER_THAN_EQUAL: // >=
            case LESS_THAN: // <
            case LESS_THAN_EQUAL: // <=
                // comparable constraint: lhs <: rhs, or rhs <: lhs
                if (!(atypeFactory.getQualifierHierarchy().isSubtype(lhsAM, rhsAM)
                        || atypeFactory.getQualifierHierarchy().isSubtype(rhsAM, lhsAM))) {
                    checker.report(
                            Result.failure(
                                    "comparison.unit.mismatch",
                                    atypeFactory
                                            .getAnnotationFormatter()
                                            .formatAnnotationMirror(lhsAM),
                                    atypeFactory
                                            .getAnnotationFormatter()
                                            .formatAnnotationMirror(rhsAM)),
                            binaryTree);
                }
                // if (!AnnotationUtils.areSame(lhsAM, rhsAM)) {
                // }
            default:
                break;
        }

        return super.visitBinary(binaryTree, p);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        ExpressionTree var = node.getVariable();
        ExpressionTree expr = node.getExpression();
        AnnotatedTypeMirror varType = atypeFactory.getAnnotatedType(var);
        AnnotatedTypeMirror exprType = atypeFactory.getAnnotatedType(expr);

        Kind kind = node.getKind();

        if ((kind == Kind.PLUS_ASSIGNMENT || kind == Kind.MINUS_ASSIGNMENT)) {
            if (!atypeFactory.getTypeHierarchy().isSubtype(exprType, varType)) {
                checker.report(
                        Result.failure("compound.assignment.type.incompatible", varType, exprType),
                        node);
            }
        } else if (exprType.getAnnotation(UnknownUnits.class) == null) {
            // Only allow mul/div with unqualified units
            checker.report(
                    Result.failure("compound.assignment.type.incompatible", varType, exprType),
                    node);
        }

        return null; // super.visitCompoundAssignment(node, p);
    }

    // We implicitly set DIMENSIONLESS as the type of all throwable and exceptions
    // We update the lower bounds here
    @Override
    protected Set<? extends AnnotationMirror> getExceptionParameterLowerBoundAnnotations() {
        Set<AnnotationMirror> lowerBounds = AnnotationUtils.createAnnotationSet();
        lowerBounds.add(unitsRepUtils.DIMENSIONLESS);
        return lowerBounds;
    }
}
