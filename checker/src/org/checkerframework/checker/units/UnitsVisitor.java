package org.checkerframework.checker.units;

import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree;
import org.checkerframework.checker.units.qual.Scalar;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;

/**
 * Units visitor.
 */
public class UnitsVisitor extends BaseTypeVisitor<UnitsAnnotatedTypeFactory> {
    public UnitsVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        // Triggers the math check for compound assignment operations
        AnnotatedTypeMirror varType = atypeFactory.getAnnotatedType(node.getVariable());
        AnnotatedTypeMirror exprType = atypeFactory.getAnnotatedType(node.getExpression());
        Tree.Kind kind = node.getKind();

        atypeFactory
                .getUnitsMathOperatorsRelations()
                .processCompoundAssignmentOperation(node, kind, null, varType, exprType);

        return null;
    }

    // Allow references to be declared using any units annotation except
    // UnitsBottom. Classes are by default Scalar, but these reference
    // declarations will use some unit that isn't a subtype of Scalar.
    // If this override isn't in place, then a lot of Type.Invalid errors show:
    //    tests/all-systems/TypeVarInstanceOf.java:3: error: [type.invalid] [@UnknownUnits] may not be applied to the type "@UnknownUnits Object"
    //    public static <T> void clone(final T obj) {
    //                   ^

    @Override
    public boolean isValidUse(
            AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        // eg for the statement "@m Double x;" the declarationType is @Scalar
        // Double, and the useType is @m Double
        if (declarationType.getEffectiveAnnotation(Scalar.class) != null
                && useType.getEffectiveAnnotation(UnitsBottom.class) == null) {
            // if declared type of a class is Scalar, and the use of that class
            // is any of the Units annotations other than UnitsBottom, return
            // true
            return true;
        } else {
            // otherwise check the usage using super
            return super.isValidUse(declarationType, useType, tree);
        }
    }

    //    // Allow the creation of objects using any units annotation except
    //    // UnitsBottom. Classes are by default Scalar, but these objects may use
    //    // some unit that isn't a subtype of Scalar.
    //    @Override
    //    protected boolean checkConstructorInvocation(
    //            AnnotatedDeclaredType invocation,
    //            AnnotatedExecutableType constructor,
    //            NewClassTree newClassTree) {
    //        // The declared constructor return type is the same as the declared type
    //        // of the class that is being constructed, by default this will be UnknownUnits.
    //        // For Boxed Number types, we have @PolyUnit for the constructor return type which will
    //        // match the unit of the single number parameter of the constructor.
    //        // eg for the statement "new @m Double(30.0);" the constructor return type is
    //        // @Scalar Double while the declared use type is @m Double.
    //        AnnotatedTypeMirror declaredConstructorReturnType = constructor.getReturnType();
    //
    //        // TODO(jyluo): do it for all classes that are declared @UnknownUnits?
    //
    //        // If it is a boxed primitive class, and the constructor return type is scalar, and the
    //        // use type is any units annotation except UnitsBottom, pass.
    //        if (TypesUtils.isBoxedPrimitive(declaredConstructorReturnType.getUnderlyingType())
    //                && declaredConstructorReturnType.getEffectiveAnnotation(Scalar.class) != null
    //                && invocation.getEffectiveAnnotation(UnitsBottom.class) == null) {
    //            return true;
    //        } else {
    //            // otherwise check the constructor invocation using super
    //            return super.checkConstructorInvocation(invocation, constructor, newClassTree);
    //        }
    //    }
}
