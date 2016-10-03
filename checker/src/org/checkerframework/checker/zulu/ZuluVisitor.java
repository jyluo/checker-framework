package org.checkerframework.checker.zulu;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

/**
 * Units visitor.
 */
public class ZuluVisitor extends BaseTypeVisitor<ZuluAnnotatedTypeFactory> {
    public ZuluVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    // Allow references to be declared using any units annotation except
    // UnitsBottom. Classes are by default Scalar, but these reference
    // declarations will use some unit that isn't a subtype of Scalar.
    // If this override isn't in place, then a lot of Type.Invalid errors show:
    //    tests/all-systems/TypeVarInstanceOf.java:3: error: [type.invalid] [@UnknownUnits] may not be applied to the type "@UnknownUnits Object"
    //    public static <T> void clone(final T obj) {
    //                   ^

    //    @Override
    //    public boolean isValidUse(
    //            AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
    //        // eg for the statement "@m Double x;" the declarationType is @Scalar
    //        // Double, and the useType is @m Double
    //        if (declarationType.getEffectiveAnnotation(ZuluMIDDLE.class) != null
    //                        && useType.getEffectiveAnnotation(ZuluBOTTOM.class) == null) {
    //            // if declared type of a class is Scalar, and the use of that class
    //            // is any of the Units annotations other than UnitsBottom, return
    //            // true
    //            return true;
    //        } else {
    //            // otherwise check the usage using super
    //            return super.isValidUse(declarationType, useType, tree);
    //        }
    //    }

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
