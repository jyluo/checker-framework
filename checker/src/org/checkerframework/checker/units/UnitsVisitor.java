package org.checkerframework.checker.units;

import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import org.checkerframework.checker.units.qual.Scalar;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TypesUtils;

/**
 * Units visitor.
 */
public class UnitsVisitor extends BaseTypeVisitor<UnitsAnnotatedTypeFactory> {
    public UnitsVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        AnnotatedTypeMirror varType = atypeFactory.getAnnotatedType(node.getVariable());
        AnnotatedTypeMirror exprType = atypeFactory.getAnnotatedType(node.getExpression());
        Tree.Kind kind = node.getKind();

        atypeFactory
                .getUnitsMathOperatorsRelations()
                .processCompoundAssignmentOperation(node, kind, null, varType, exprType);

        return null;
    }
    //
    //    // Allow references to be declared using any units annotation except
    //    // UnitsBottom. Classes are by default Scalar, but these reference
    //    // declarations will use some unit that isn't a subtype of Scalar.
    //    @Override
    //    public boolean isValidUse(
    //            AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
    //        // eg for the statement "@m Double x;" the declarationType is @Scalar
    //        // Double, and the useType is @m Double
    //        if (isValidDeclarationTypeUse(declarationType, useType)) {
    //            // if declared type of a class is Scalar, and the use of that class
    //            // is any of the Units annotations other than UnitsBottom, return
    //            // true
    //            return true;
    //        } else {
    //            // otherwise check the usage using super
    //            return super.isValidUse(declarationType, useType, tree);
    //        }
    //    }
    //
    // Allow the creation of objects using any units annotation except
    // UnitsBottom. Classes are by default Scalar, but these objects may use
    // some unit that isn't a subtype of Scalar.
    @Override
    protected boolean checkConstructorInvocation(
            AnnotatedDeclaredType invocation,
            AnnotatedExecutableType constructor,
            NewClassTree newClassTree) {
        // The declared constructor return type is the same as the declared type
        // of the class that is being constructed, by default this will be UnknownUnits.
        // For Boxed Number types, we have @PolyUnit for the constructor return type which will
        // match the unit of the single number parameter of the constructor.
        // eg for the statement "new @m Double(30.0);" the constructor return type is
        // @Scalar Double while the declared use type is @m Double.
        AnnotatedTypeMirror declaredConstructorReturnType = constructor.getReturnType();

        // TODO(jyluo): do it for all classes that are declared @UnknownUnits?

        // If it is a boxed primitive class, and the constructor return type is scalar, and the
        // use type is any units annotation except UnitsBottom, pass.
        if (TypesUtils.isBoxedPrimitive(declaredConstructorReturnType.getUnderlyingType())
                && declaredConstructorReturnType.getEffectiveAnnotation(Scalar.class) != null
                && invocation.getEffectiveAnnotation(UnitsBottom.class) == null) {
            return true;
        } else {
            // otherwise check the constructor invocation using super
            return super.checkConstructorInvocation(invocation, constructor, newClassTree);
        }
        //
        //
        //        // If the use type is @UnknownUnits but that annotation was never explicitly written in
        //        // the code for a new class instance, then we allow the newly created boxed primitive object
        //        // to take on the unit of the value that was passed into it's constructor (from constructor
        //        // type).
        //
        //
        //        System.out.println("=== constructor type: " + declaredConstructorReturnType.getEffectiveAnnotations());
        //        System.out.println("=== use type: " + invocation.getEffectiveAnnotations());
        //        System.out.println("=== new class tree: " + declaredConstructorReturnType.getUnderlyingType());
        //        System.out.println("=== is one of the boxed number types: " + TypesUtils.isBoxedPrimitive(declaredConstructorReturnType.getUnderlyingType()));
        //        System.out.println("== explicit annos: " + atypeFactory.fromNewClass(newClassTree).getEffectiveAnnotations());
        //        System.out.println();
        //
        //        if (invocation.getEffectiveAnnotation(UnknownUnits.class) != null &&
        //                        atypeFactory.fromNewClass(newClassTree).getEffectiveAnnotation(UnknownUnits.class) == null) {
        //            System.out.println("use type is uknown and anno was never written in source");
        //            // set the use type to be the same as the constructor type
        //            invocation.replaceAnnotations(declaredConstructorReturnType.getAnnotations());
        //            final AnnotatedDeclaredType typeFromClassTree = atypeFactory.fromNewClass(newClassTree);
        //            typeFromClassTree.replaceAnnotations(declaredConstructorReturnType.getAnnotations());
        //
        //            System.out.println("=== updated use type: " + invocation.getEffectiveAnnotations());
        //            System.out.println("=== updated use type: " + typeFromClassTree.getEffectiveAnnotations());
        //            System.out.println();
        //
        //            return true;
        //        }
        //
        //
        //        System.out.println();

        //        atypeFactory.getAnnotatedType(newClassTree.getIdentifier()).getAnnotations()

        //
        //        AnnotatedDeclaredType returnType = (AnnotatedDeclaredType) constructor.getReturnType();
        //        // When an interface is used as the identifier in an anonymous class (e.g. new Comparable() {})
        //        // the constructor method will be Object.init() {} which has an Object return type
        //        // When TypeHierarchy attempts to convert it to the supertype (e.g. Comparable) it will return
        //        // null from asSuper and return false for the check.  Instead, copy the primary annotations
        //        // to the declared type and then do a subtyping check
        //        if (TypesUtils.isBoxedPrimitive(declaredConstructorReturnType.getUnderlyingType())) {
        //            final AnnotatedDeclaredType retAsDt = invocation.deepCopy();
        //            retAsDt.replaceAnnotations(returnType.getAnnotations());
        //            returnType = retAsDt;

        //        // If a class is declared as UnknownUnits, and the use of the class is any
        //        // units annotation except UnitsBottom, return true
        //        if (TypesUtils.isBoxedPrimitive(declaredConstructorReturnType.getUnderlyingType())
        //                        //declaredConstructorReturnType.getEffectiveAnnotation(UnknownUnits.class) != null
        //                        && invocation.getEffectiveAnnotation(UnitsBottom.class) == null) {
        //            return true;
        //        } else {
        //            // otherwise check the constructor invocation using super
        //            return super.checkConstructorInvocation(invocation, constructor, newClassTree);
        //        }
    }

    //    // allow the passing of scalar number literals into method parameters that
    //    // require a unit. all parameters are scalar by default.
    //    // Developer Notes: keep in sync with super implementation.
    //    @Override
    //    protected void checkArguments(
    //            List<? extends AnnotatedTypeMirror> requiredArgs,
    //            List<? extends ExpressionTree> passedArgs) {
    //        assert requiredArgs.size() == passedArgs.size()
    //                : "mismatch between required args ("
    //                        + requiredArgs
    //                        + ") and passed args ("
    //                        + passedArgs
    //                        + ")";
    //
    //        Pair<Tree, AnnotatedTypeMirror> preAssCtxt = visitorState.getAssignmentContext();
    //        try {
    //            for (int i = 0; i < requiredArgs.size(); ++i) {
    //                visitorState.setAssignmentContext(
    //                        Pair.<Tree, AnnotatedTypeMirror>of(
    //                                (Tree) null, (AnnotatedTypeMirror) requiredArgs.get(i)));
    //
    //                // Units Checker Code =======================
    //                AnnotatedTypeMirror requiredArg = requiredArgs.get(i);
    //                ExpressionTree passedExpression = passedArgs.get(i);
    //                AnnotatedTypeMirror passedArg = atypeFactory.getAnnotatedType(passedExpression);
    //
    //                if (UnitsRelationsTools.hasSpecificUnit(passedArg, atypeFactory.scalar)
    //                        && UnitsRelationsTools.isPrimitiveNumberLiteralExpression(passedExpression)
    //                        && !UnitsRelationsTools.hasSpecificUnit(requiredArg, atypeFactory.BOTTOM)) {
    //                    // if the method argument is a scalar number literal, or a
    //                    // numerical expression consisting only of scalar literals,
    //                    // and the method parameter has any unit other than
    //                    // UnitsBottom, pass, as those literals are tied to
    //                    // those method calls and can be safely assumed to take on
    //                    // the unit of the parameter. Scalar variables passed to
    //                    // such methods still result in errors.
    //                } else {
    //                    // Developer note: keep in sync with super implementation
    //                    commonAssignmentCheck(
    //                            requiredArg, passedExpression, "argument.type.incompatible");
    //                }
    //                // End Units Checker Code ===================
    //
    //                // Also descend into the argument within the correct assignment
    //                // context.
    //                scan(passedArgs.get(i), null);
    //            }
    //        } finally {
    //            visitorState.setAssignmentContext(preAssCtxt);
    //        }
    //    }
    //
    //    // allow the invocation of a method defined in a Scalar class on an
    //    // UnknownUnits object (all classes are scalar by default)
    //    // Developer Notes: keep in sync with super implementation.
    //    @Override
    //    protected void checkMethodInvocability(
    //            AnnotatedExecutableType method, MethodInvocationTree node) {
    //        if (method.getReceiverType() == null) {
    //            // Static methods don't have a receiver.
    //            return;
    //        }
    //        if (method.getElement().getKind() == ElementKind.CONSTRUCTOR) {
    //            // TODO: Explicit "this()" calls of constructors have an implicit
    //            // passed
    //            // from the enclosing constructor. We must not use the self type,
    //            // but
    //            // instead should find a way to determine the receiver of the
    //            // enclosing constructor.
    //            // rcv =
    //            // ((AnnotatedExecutableType)atypeFactory.getAnnotatedType(atypeFactory.getEnclosingMethod(node))).getReceiverType();
    //            return;
    //        }
    //
    //        AnnotatedTypeMirror methodReceiver = method.getReceiverType().getErased();
    //        AnnotatedTypeMirror treeReceiver = methodReceiver.shallowCopy(false);
    //        AnnotatedTypeMirror rcv = atypeFactory.getReceiverType(node);
    //
    //        treeReceiver.addAnnotations(rcv.getEffectiveAnnotations());
    //
    //        if (skipReceiverSubtypeCheck(node, methodReceiver, rcv)) {
    //            return;
    //        }
    //
    //        // Units Checker Code =======================
    //        // if the method's declared receiver is Scalar and the receiving object is
    //        // UnknownUnits, or if the method's class is Object, pass
    //        if (UnitsRelationsTools.hasSpecificUnit(methodReceiver, atypeFactory.scalar)
    //                        && UnitsRelationsTools.hasSpecificUnit(treeReceiver, atypeFactory.TOP)
    //                || TypesUtils.isObject(methodReceiver.getUnderlyingType())) {
    //            return;
    //        }
    //        // End Units Checker Code ===================
    //
    //        if (!atypeFactory.getTypeHierarchy().isSubtype(treeReceiver, methodReceiver)) {
    //            checker.report(
    //                    Result.failure(
    //                            "method.invocation.invalid",
    //                            TreeUtils.elementFromUse(node),
    //                            treeReceiver.toString(),
    //                            methodReceiver.toString()),
    //                    node);
    //        }
    //    }
}
