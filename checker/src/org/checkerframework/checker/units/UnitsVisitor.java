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

/** Units visitor. */
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

    // NOTE: TEMPORARY OVERRIDE
    // Allow references of boxed primitives to be declared using any units annotation except
    // UnitsBottom. Classes are by default Scalar, but these reference
    // declarations will use some unit that isn't a subtype of Scalar.
    // If this override isn't in place, then a lot of Type.Invalid errors show:
    //    tests/all-systems/TypeVarInstanceOf.java:3: error: [type.invalid] [@UnknownUnits] may not be applied to the type "@UnknownUnits Object"
    //    public static <T> void clone(final T obj) {
    //                   ^
    // TODO: adapt to mier's work.
    @Override
    public boolean isValidUse(
            AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        // eg for the statement "@m Double x;" the declarationType is @Scalar
        // Double, and the useType is @m Double
        if (declarationType.getEffectiveAnnotation(Scalar.class) != null
                && useType.getEffectiveAnnotation(UnitsBottom.class) == null
        // && isBoxedNumberPrimitive(declarationType.getUnderlyingType())
        ) {
            // if declared type of a class is Scalar, and the use of that class
            // is any of the Units annotations other than UnitsBottom, return
            // true
            return true;
        } else {
            // otherwise check the usage using super
            return super.isValidUse(declarationType, useType, tree);
        }
    }

    // NOTE: TEMPORARY OVERRIDE
    // Allow the creation of boxed primitive objects using any units annotation except
    // UnitsBottom. Classes are by default Scalar, but these objects may use
    // some unit that isn't a subtype of Scalar.
    //
    // TODO: adapt to mier's work.
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

        // If it is a boxed primitive class, and the constructor return type is scalar, and the
        // use type is any units annotation except UnitsBottom, pass.
        if (declaredConstructorReturnType.getEffectiveAnnotation(Scalar.class) != null
                && invocation.getEffectiveAnnotation(UnitsBottom.class) == null
        // && isBoxedNumberPrimitive(declaredConstructorReturnType.getUnderlyingType())
        ) {
            return true;
        } else {
            // otherwise check the constructor invocation using super
            return super.checkConstructorInvocation(invocation, constructor, newClassTree);
        }
    }

    /*
    public static boolean isBoxedNumberPrimitive(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }

        String qualifiedName = TypesUtils.getQualifiedName((DeclaredType) type).toString();

        return (qualifiedName.equals("java.lang.Object")
                        || qualifiedName.equals("java.lang.Number")
                        || qualifiedName.equals("java.lang.Byte")
                        || qualifiedName.equals("java.lang.Short")
                        || qualifiedName.equals("java.lang.Integer")
                        || qualifiedName.equals("java.lang.Long")
                        || qualifiedName.equals("java.lang.Double")
                        || qualifiedName.equals("java.lang.Float"));
    }*/
}
