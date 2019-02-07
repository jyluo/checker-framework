package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UnaryTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.UnitsAddition;
import org.checkerframework.checker.units.qual.UnitsDivision;
import org.checkerframework.checker.units.qual.UnitsMultiplication;
import org.checkerframework.checker.units.qual.UnitsSame;
import org.checkerframework.checker.units.qual.UnitsSames;
import org.checkerframework.checker.units.qual.UnitsSubtraction;
import org.checkerframework.checker.units.utils.UnitsRepresentationUtils;
import org.checkerframework.checker.units.utils.UnitsTypecheckUtils;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory.ParameterizedMethodType;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.UserError;

/**
 * Units visitor.
 *
 * <p>Ensure consistent use of compound assignments.
 */
public class UnitsVisitor extends BaseTypeVisitor<UnitsAnnotatedTypeFactory> {
    /** reference to the units representation utilities library */
    protected final UnitsRepresentationUtils unitsRepUtils;

    /** reference to the units type check utilities library */
    protected final UnitsTypecheckUtils unitsTypecheckUtils;

    public UnitsVisitor(BaseTypeChecker checker) {
        super(checker);
        unitsRepUtils = atypeFactory.getUnitsRepresentationUtils();
        unitsTypecheckUtils = atypeFactory.getUnitsTypecheckUtils();
    }

    /** override to allow uses of classes declared as {@link Dimensionless} with units */
    @Override
    public boolean isValidUse(
            AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        AnnotatedDeclaredType erasedDeclaredType = declarationType.getErased();
        AnnotationMirror anno =
                erasedDeclaredType.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);
        return AnnotationUtils.areSame(anno, unitsRepUtils.DIMENSIONLESS)
                || super.isValidUse(declarationType, useType, tree);
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

    // TODO: check this rule
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
        } else if (AnnotationUtils.areSame(
                exprType.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP), unitsRepUtils.TOP)) {
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

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        ParameterizedMethodType mType = atypeFactory.methodFromUse(node);
        AnnotatedExecutableType invokedMethod = mType.methodType;
        // TreeUtils.elementFromUse(node)
        ExecutableElement methodElement = invokedMethod.getElement();
        // List<AnnotatedTypeMirror> typeargs = mType.typeArgs;

        // System.err.println(" invokedMethod " + invokedMethod.getErased());
        // System.err.println(" methodElement " + methodElement);

        // Build up a list of ATMs corresponding to the index convention used in the Units
        // method meta-annotations. null values are inserted if there is no possible ATM for
        // that index position.
        List<AnnotatedTypeMirror> atms = new ArrayList<>();
        atms.add(atypeFactory.getAnnotatedType(node));

        boolean isStaticMethod = methodElement.getModifiers().contains(Modifier.STATIC);
        // System.err.println(" isStaticMethod " + isStaticMethod);

        ExpressionTree receiver = TreeUtils.getReceiverTree(node);
        // System.err.println(" receiver " + receiver);

        // ATM for argument to the formal "this" parameter
        if (receiver != null && !isStaticMethod) {
            AnnotatedTypeMirror receiverATM = atypeFactory.getAnnotatedType(receiver);
            atms.add(receiverATM);
        } else {
            atms.add(null);
        }

        for (ExpressionTree arg : node.getArguments()) {
            AnnotatedTypeMirror argATM = atypeFactory.getAnnotatedType(arg);
            // System.err.println(" arg " + arg + " type " + argATM);
            atms.add(argATM);
        }

        // multiple meta-annotations are allowed on each method
        for (AnnotationMirror anno : atypeFactory.getDeclAnnotations(methodElement)) {
            if (AnnotationUtils.areSameByClass(anno, UnitsAddition.class)) {
                checkUnitsAsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSubtraction.class)) {
                checkUnitsAsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsMultiplication.class)) {
                checkUnitsAsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsDivision.class)) {
                checkUnitsAsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSames.class)) {
                for (AnnotationMirror same :
                        AnnotationUtils.getElementValueArray(
                                anno, "value", AnnotationMirror.class, false)) {
                    checkUnitsAsSame(node, invokedMethod, same, atms);
                }
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSame.class)) {
                checkUnitsAsSame(node, invokedMethod, anno, atms);
            }
        }

        return super.visitMethodInvocation(node, p);
    }

    protected void checkUnitsAsArithmetic(
            MethodInvocationTree node,
            AnnotatedExecutableType invokedMethod,
            AnnotationMirror anno,
            List<AnnotatedTypeMirror> atms) {
        int leftOperandPos = unitsTypecheckUtils.getIntElementValue(anno, "larg");
        int rightOperandPos = unitsTypecheckUtils.getIntElementValue(anno, "rarg");
        int resultPos = unitsTypecheckUtils.getIntElementValue(anno, "res");

        // The check is done here instead of visitMethod() in case an improper meta-annotation was
        // declared in a stub
        validatePositionIndex(invokedMethod, anno, leftOperandPos);
        validatePositionIndex(invokedMethod, anno, rightOperandPos);
        validatePositionIndex(invokedMethod, anno, resultPos);

        if (leftOperandPos == rightOperandPos) {
            throw new UserError(
                    "The indices larg and rarg cannot be the same for meta-annotation "
                            + anno
                            + " declared on method "
                            + invokedMethod);
        }
    }

    protected void checkUnitsAsSame(
            MethodInvocationTree node,
            AnnotatedExecutableType invokedMethod,
            AnnotationMirror same,
            List<AnnotatedTypeMirror> atms) {
        int fstPos = unitsTypecheckUtils.getIntElementValue(same, "fst");
        int sndPos = unitsTypecheckUtils.getIntElementValue(same, "snd");

        // The check is done here instead of visitMethod() in case an improper meta-annotation was
        // declared in a stub
        validatePositionIndex(invokedMethod, same, fstPos);
        validatePositionIndex(invokedMethod, same, sndPos);

        if (fstPos == sndPos) {
            throw new UserError(
                    "The indices fst and snd cannot be the same for meta-annotation "
                            + same
                            + " declared on method "
                            + invokedMethod);
        }

        AnnotatedTypeMirror fst = atms.get(fstPos + 1);
        AnnotatedTypeMirror snd = atms.get(sndPos + 1);

        AnnotationMirror fstAM = fst.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);
        AnnotationMirror sndAM = snd.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);

        if (fstPos != -1 && sndPos != -1 && !unitsTypecheckUtils.unitsEqual(fstAM, sndAM)) {
            checker.report(Result.failure("units.differ", fst, snd), node);
        }
    }

    // TODO: varargs
    protected void validatePositionIndex(
            AnnotatedExecutableType invokedMethod, AnnotationMirror same, int pos) {
        boolean lowerBoundValid = -1 <= pos;
        boolean upperBoundValid = pos <= invokedMethod.getElement().getParameters().size();

        if (!lowerBoundValid || (!invokedMethod.isVarArgs() && !upperBoundValid)) {
            throw new UserError(
                    "The index "
                            + pos
                            + " is invalid for meta-annotation "
                            + same
                            + " declared on method "
                            + invokedMethod);
        }
    }
}
