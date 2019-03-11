package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.UnitsAddition;
import org.checkerframework.checker.units.qual.UnitsCompare;
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
import org.checkerframework.framework.type.AnnotatedTypeFactory.ParameterizedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;
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
                checkCompare(binaryTree, lhsAM, rhsAM);
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
    public Void visitNewClass(NewClassTree node, Void p) {
        super.visitNewClass(node, p);

        ParameterizedExecutableType mType = atypeFactory.constructorFromUse(node);
        AnnotatedExecutableType invokedMethod = mType.executableType;
        ExecutableElement methodElement = invokedMethod.getElement();
        // List<AnnotatedTypeMirror> typeargs = mType.typeArgs;

        // System.err.println(" methodElement " + methodElement);

        // Build up a list of ATMs corresponding to the index convention used in the Units
        // method meta-annotations. null values are inserted if there is no possible ATM for
        // that index position.
        List<AnnotatedTypeMirror> atms = new ArrayList<>();
        atms.add(atypeFactory.getAnnotatedType(node));

        ExpressionTree receiver = TreeUtils.getReceiverTree(node);
        // System.err.println(" receiver " + receiver);

        // ATM for argument to the formal "this" parameter
        if (receiver != null) {
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
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSubtraction.class)) {
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsMultiplication.class)) {
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsDivision.class)) {
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSames.class)) {
                for (AnnotationMirror same :
                        AnnotationUtils.getElementValueArray(
                                anno, "value", AnnotationMirror.class, false)) {
                    checkMethodUnitsSame(node, invokedMethod, same, atms);
                }
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSame.class)) {
                checkMethodUnitsSame(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsCompare.class)) {
                checkMethodUnitsCompare(node, invokedMethod, anno, atms);
            }
        }

        return null;
    }

    // Override to not issue "cast.unsafe.constructor.invocation" warnings for classes declared
    // as @UnknownUnits as it is a common use case in Units checker.
    @Override
    protected boolean checkConstructorInvocation(
            AnnotatedDeclaredType invocation,
            AnnotatedExecutableType constructor,
            NewClassTree newClassTree) {

        // copied from super implementation
        AnnotatedDeclaredType returnType = (AnnotatedDeclaredType) constructor.getReturnType();
        // When an interface is used as the identifier in an anonymous class (e.g. new Comparable()
        // {}) the constructor method will be Object.init() {} which has an Object return type When
        // TypeHierarchy attempts to convert it to the supertype (e.g. Comparable) it will return
        // null from asSuper and return false for the check. Instead, copy the primary annotations
        // to the declared type and then do a subtyping check.
        if (invocation.getUnderlyingType().asElement().getKind().isInterface()
                && TypesUtils.isObject(returnType.getUnderlyingType())) {
            final AnnotatedDeclaredType retAsDt = invocation.deepCopy();
            retAsDt.replaceAnnotations(returnType.getAnnotations());
            returnType = retAsDt;
        }

        if (AnnotationUtils.areSame(
                returnType.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP),
                unitsRepUtils.TOP)) {
            return true;
        } else {
            return super.checkConstructorInvocation(invocation, constructor, newClassTree);
        }
    }

    // Because units permits subclasses to return objects with units, giving a
    // "super.invocation.invalid" warning at every declaration of a subclass constructor is annoying
    // to user of units, we override the check here to always permit the invocation of a super
    // constructor returning dimensionless values
    @Override
    protected void checkSuperConstructorCall(MethodInvocationTree node) {
        if (!TreeUtils.isSuperCall(node)) {
            return;
        }
        TreePath path = atypeFactory.getPath(node);
        MethodTree enclosingMethod = TreeUtils.enclosingMethod(path);
        if (TreeUtils.isConstructor(enclosingMethod)) {
            AnnotatedTypeMirror superType = atypeFactory.getAnnotatedType(node);
            AnnotationMirror superTypeMirror =
                    superType.getAnnotationInHierarchy(unitsRepUtils.TOP);
            if (!AnnotationUtils.areSame(superTypeMirror, unitsRepUtils.DIMENSIONLESS)) {
                super.checkSuperConstructorCall(node);
            }
        }
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        super.visitMethodInvocation(node, p);

        ParameterizedExecutableType mType = atypeFactory.methodFromUse(node);
        AnnotatedExecutableType invokedMethod = mType.executableType;
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
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSubtraction.class)) {
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsMultiplication.class)) {
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsDivision.class)) {
                checkMethodUnitsArithmetic(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSames.class)) {
                for (AnnotationMirror same :
                        AnnotationUtils.getElementValueArray(
                                anno, "value", AnnotationMirror.class, false)) {
                    checkMethodUnitsSame(node, invokedMethod, same, atms);
                }
            } else if (AnnotationUtils.areSameByClass(anno, UnitsSame.class)) {
                checkMethodUnitsSame(node, invokedMethod, anno, atms);
            } else if (AnnotationUtils.areSameByClass(anno, UnitsCompare.class)) {
                checkMethodUnitsCompare(node, invokedMethod, anno, atms);
            }
        }

        return null;
    }

    protected void checkMethodUnitsArithmetic(
            Tree node,
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

        if (resultPos == leftOperandPos) {
            throw new UserError(
                    "The indices res and larg cannot be the same for meta-annotation "
                            + anno
                            + " declared on method "
                            + invokedMethod);
        }

        if (resultPos == rightOperandPos) {
            throw new UserError(
                    "The indices res and rarg cannot be the same for meta-annotation "
                            + anno
                            + " declared on method "
                            + invokedMethod);
        }

        if (leftOperandPos == rightOperandPos) {
            throw new UserError(
                    "The indices larg and rarg cannot be the same for meta-annotation "
                            + anno
                            + " declared on method "
                            + invokedMethod);
        }
    }

    protected void checkMethodUnitsSame(
            Tree node,
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

    protected void checkMethodUnitsCompare(
            Tree node,
            AnnotatedExecutableType invokedMethod,
            AnnotationMirror same,
            List<AnnotatedTypeMirror> atms) {
        int fstPos = unitsTypecheckUtils.getIntElementValue(same, "fst");
        int sndPos = unitsTypecheckUtils.getIntElementValue(same, "snd");

        // TODO: for compare, -1 is not allowed
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

        checkCompare(node, fstAM, sndAM);
    }

    protected void checkCompare(Tree node, AnnotationMirror fstAM, AnnotationMirror sndAM) {
        if (!unitsTypecheckUtils.unitsComparable(atypeFactory, fstAM, sndAM)) {
            checker.report(
                    Result.failure(
                            "comparison.unit.mismatch",
                            atypeFactory.getAnnotationFormatter().formatAnnotationMirror(fstAM),
                            atypeFactory.getAnnotationFormatter().formatAnnotationMirror(sndAM)),
                    node);
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
