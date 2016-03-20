package org.checkerframework.checker.units;

import org.checkerframework.checker.units.qual.Scalar;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeValidator;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.basetype.TypeValidator;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;

/**
 * Units visitor.
 */
public class UnitsVisitor extends BaseTypeVisitor<UnitsAnnotatedTypeFactory> {
    protected final AnnotationMirror scalar = AnnotationUtils.fromClass(elements, Scalar.class);
    protected final AnnotationMirror TOP = AnnotationUtils.fromClass(elements, UnknownUnits.class);
    protected final AnnotationMirror BOTTOM = AnnotationUtils.fromClass(elements, UnitsBottom.class);

    private final TypeMirror stringType;
    //private final TypeMirror objectType;

    public UnitsVisitor(BaseTypeChecker checker) {
        super(checker);

        stringType = checker.getElementUtils().getTypeElement(java.lang.String.class.getCanonicalName()).asType();
        //objectType = checker.getElementUtils().getTypeElement(java.lang.Object.class.getCanonicalName()).asType();
    }

    // Override to allow references to be declared using any units annotation
    // except UnitsBottom. Classes are by default Scalar, but these reference
    // declarations will use some unit that isn't a subtype of Scalar.
    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        // eg for the statement "@m Double x;" the declarationType is @Scalar
        // Double, and the useType is @m Double
        if (isValidDeclarationTypeUse(declarationType, useType)) {
            // if declared type of a class is Scalar, and the use of that class
            // is any of the Units annotations other than UnitsBottom, return
            // true
            return true;
        } else {
            // otherwise check the usage using super
            return super.isValidUse(declarationType, useType, tree);
        }
    }

    // Override to allow the creation of objects using any units annotation
    // except UnitsBottom. Classes are by default Scalar, but these objects may
    // use some unit that isn't a subtype of Scalar.
    @Override
    protected boolean checkConstructorInvocation(AnnotatedDeclaredType useType, AnnotatedExecutableType constructor, Tree src) {
        // The declared constructor return type is the same as the declared type
        // of the class that is being constructed, by default this will be
        // Scalar.
        // eg for the statement "new @m Double(30.0);" the return type is
        // @Scalar
        // Double while the declared use type is @m Double.
        AnnotatedTypeMirror declaredConstructorReturnType = constructor.getReturnType();

        if (isValidDeclarationTypeUse(declaredConstructorReturnType, useType)) {
            return true;
        } else {
            // otherwise check the constructor invocation using super
            return super.checkConstructorInvocation(useType, constructor, src);
        }
    }

    // If a class is declared as Scalar, and the use of the class is any units
    // annotation except UnitsBottom, then return true
    private boolean isValidDeclarationTypeUse(AnnotatedTypeMirror declaredType, AnnotatedTypeMirror useType) {
        return declaredType.getEffectiveAnnotation(Scalar.class) != null &&
                useType.getEffectiveAnnotation(UnitsBottom.class) == null;
    }

    // Allow the passing of UnknownUnits number literals into Scalar method
    // parameters. all parameters are scalar by default.
    // Developer Notes: keep in sync with super implementation.
    @Override
    protected void checkArguments(List<? extends AnnotatedTypeMirror> requiredArgs, List<? extends ExpressionTree> passedArgs) {
        assert requiredArgs.size() == passedArgs.size() : "mismatch between required args (" + requiredArgs +
                ") and passed args (" + passedArgs + ")";

        Pair<Tree, AnnotatedTypeMirror> preAssCtxt = visitorState.getAssignmentContext();
        try {
            for (int i = 0; i < requiredArgs.size(); ++i) {
                visitorState.setAssignmentContext(Pair.<Tree, AnnotatedTypeMirror>of((Tree) null, (AnnotatedTypeMirror) requiredArgs.get(i)));

                // Units Checker Code =======================
                AnnotatedTypeMirror requiredArg = requiredArgs.get(i);
                ExpressionTree passedExpression = passedArgs.get(i);
                AnnotatedTypeMirror passedArg = atypeFactory.getAnnotatedType(passedExpression);

                if (UnitsRelationsTools.hasSpecificUnit(requiredArg, scalar)
                        && UnitsRelationsTools.hasSpecificUnit(passedArg, TOP)
                        && isPrimitiveNumberLiteralExpression(passedExpression)) {
                    // if the method parameter is Scalar, and the passed in
                    // argument is an UnknownUnits mathematical expression that
                    // consists of only number literals pass the check

                    //} else if (UnitsRelationsTools.hasSpecificUnit(requiredArg, scalar) && isSameUnderlyingType(requiredArg.getUnderlyingType(), objectType)) {
                    //    // if the method parameter is Scalar Object, pass regardless of what the argument is as Object accepts anything
                } else {
                    // Developer note: keep in sync with super implementation
                    commonAssignmentCheck(requiredArg, passedExpression, "argument.type.incompatible", false);
                }
                // End Units Checker Code ===================

                // Also descend into the argument within the correct assignment
                // context.
                scan(passedArgs.get(i), null);
            }
        } finally {
            visitorState.setAssignmentContext(preAssCtxt);
        }
    }

    // checks to see if an entire mathematical expression consists of only
    // number literals
    private boolean isPrimitiveNumberLiteralExpression(ExpressionTree tree) {
        // TODO: bitshift support?
        switch (tree.getKind()) {
        case TYPE_CAST:
            // any type casting of the number literals are ignored
            return isPrimitiveNumberLiteralExpression(((TypeCastTree) tree).getExpression());
        case PARENTHESIZED:
            // descend into parentheses
            return isPrimitiveNumberLiteralExpression(((ParenthesizedTree) tree).getExpression());
        case PLUS:
        case MINUS:
        case MULTIPLY:
        case DIVIDE:
        case REMAINDER:
            // for the 5 mathematical operations, return true if both operands
            // are mathematical sub-expression that consists of only number
            // literals
            BinaryTree bTree = (BinaryTree) tree;
            return isPrimitiveNumberLiteralExpression(bTree.getLeftOperand())
                    && isPrimitiveNumberLiteralExpression(bTree.getRightOperand());
        case INT_LITERAL:
        case LONG_LITERAL:
        case FLOAT_LITERAL:
        case DOUBLE_LITERAL:
            return true;
        default:
            return false;
        }
    }

    // allow the invocation of a method defined in a Scalar class (all classes
    // are scalar by default) on an UnknownUnits object
    // Developer Notes: keep in sync with super implementation.
    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method, MethodInvocationTree node) {
        if (method.getReceiverType() == null) {
            // Static methods don't have a receiver.
            return;
        }
        if (method.getElement().getKind() == ElementKind.CONSTRUCTOR) {
            // TODO: Explicit "this()" calls of constructors have an implicit passed
            // from the enclosing constructor. We must not use the self type, but
            // instead should find a way to determine the receiver of the enclosing constructor.
            // rcv = ((AnnotatedExecutableType)atypeFactory.getAnnotatedType(atypeFactory.getEnclosingMethod(node))).getReceiverType();
            return;
        }

        AnnotatedTypeMirror methodReceiver = method.getReceiverType().getErased();
        AnnotatedTypeMirror treeReceiver = methodReceiver.shallowCopy(false);
        AnnotatedTypeMirror rcv = atypeFactory.getReceiverType(node);

        treeReceiver.addAnnotations(rcv.getEffectiveAnnotations());

        if (skipReceiverSubtypeCheck(node, methodReceiver, rcv)) {
            return;
        }

        // Units Checker Code =======================
        // if the method receiver is Scalar and the receiving object is
        // UnknownUnits, pass
        if (UnitsRelationsTools.hasSpecificUnit(methodReceiver, scalar)
                && UnitsRelationsTools.hasSpecificUnit(treeReceiver, TOP)) {
            return;
        }
        // End Units Checker Code ===================

        if (!atypeFactory.getTypeHierarchy().isSubtype(treeReceiver, methodReceiver)) {
            checker.report(Result.failure("method.invocation.invalid", TreeUtils.elementFromUse(node), treeReceiver.toString(), methodReceiver.toString()), node);
        }
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        ExpressionTree var = node.getVariable();
        ExpressionTree expr = node.getExpression();
        AnnotatedTypeMirror varType = atypeFactory.getAnnotatedType(var);
        AnnotatedTypeMirror exprType = atypeFactory.getAnnotatedType(expr);
        Tree.Kind kind = node.getKind();

        // skip checking addition on Strings
        if (kind == Tree.Kind.PLUS_ASSIGNMENT
                && isSameUnderlyingType(varType.getUnderlyingType(), stringType)) {
            return null;
        }

        // plus and minus assignment
        if (kind == Tree.Kind.PLUS_ASSIGNMENT
                || kind == Tree.Kind.MINUS_ASSIGNMENT) {
            // if the right hand side (expr) is not a subtype of the left hand
            // side (var) then throw error
            if (!atypeFactory.getTypeHierarchy().isSubtype(exprType, varType)) {
                checker.report(Result.failure("compound.assignment.type.incompatible", varType, exprType), node);
            }
        }
        // multiply, divide, modulus assignment
        else if (kind == Tree.Kind.MULTIPLY_ASSIGNMENT
                || kind == Tree.Kind.DIVIDE_ASSIGNMENT
                || kind == Tree.Kind.REMAINDER_ASSIGNMENT) {
            if (UnitsRelationsTools.hasSpecificUnit(varType, TOP)) {
                // if the left hand side is unknown, turn it into whatever is on
                // right hand side
                varType.replaceAnnotations(exprType.getAnnotations());
            } else if (!UnitsRelationsTools.hasSpecificUnit(exprType, scalar)) {
                // if the right hand side is not a scalar then throw error
                // Only allow mul/div with unqualified units
                checker.report(Result.failure("compound.assignment.type.incompatible", varType, exprType), node);
            }
        }

        return null;
    }

    private boolean isSameUnderlyingType(TypeMirror lht, TypeMirror rht) {
        // use typeUtils.isSameType instead of TypeMirror.equals as this
        // will check only the underlying type and ignores declarations on
        // the type mirror
        return checker.getTypeUtils().isSameType(lht, rht);
    }


    @Override
    protected void checkTypeArguments(Tree toptree,
            List<? extends AnnotatedTypeParameterBounds> paramBounds,
            List<? extends AnnotatedTypeMirror> typeargs,
            List<? extends Tree> typeargTrees) {

//        if(! toptree.toString().startsWith("super")) {
//            System.out.println();
//            System.out.println("Check Type Args: ");
//            System.out.println("Tree: " + toptree);
//            System.out.println("ParamBounds: " + Arrays.toString(paramBounds.toArray()));
//            System.out.println("typeargs: " + Arrays.toString(typeargs.toArray()));
//            System.out.println("typeargTrees: " + Arrays.toString(typeargTrees.toArray()));
//        }
//

        super.checkTypeArguments(toptree, paramBounds, typeargs, typeargTrees);
    }


    @Override
    protected TypeValidator createTypeValidator() {
        return new UnitsTypeValidator(checker, this, atypeFactory);
    }

    class UnitsTypeValidator extends BaseTypeValidator {

        public UnitsTypeValidator(BaseTypeChecker checker, BaseTypeVisitor<?> visitor, AnnotatedTypeFactory atypeFactory) {
            super(checker, visitor, atypeFactory);
        }

        @Override
        protected Void visitParameterizedType(AnnotatedDeclaredType type, ParameterizedTypeTree tree) {

//            System.out.println();
//            System.out.println("Visit Parameterized Type");
//            System.out.println("declared type: " + type);
//            System.out.println("param type tree: " + tree);


            return super.visitParameterizedType(type, tree);
        }

    }
}