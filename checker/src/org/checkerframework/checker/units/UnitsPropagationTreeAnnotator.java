package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.type.TypeKind;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

public final class UnitsPropagationTreeAnnotator extends PropagationTreeAnnotator {

    UnitsAnnotatedTypeFactory factory;

    // used to store a list of UnitsClassRelations implementations to handle
    // special processing of methods in classes
    // it is keyed by the fully qualified class names of each class that
    // requires special handling of its methods
    private static final Map<String, UnitsClassRelations> classProcessors = new HashMap<>();

    public UnitsPropagationTreeAnnotator(
            UnitsChecker checker, UnitsAnnotatedTypeFactory atypeFactory) {
        super(atypeFactory);

        factory = atypeFactory;

        // Add all of the class relations here to handle special processing
        // of specific methods such as Math.pow() or Integer.compare()
        UnitsMathClassRelations javaMathClassMethodRelations =
                new UnitsMathClassRelations(checker, atypeFactory);
        classProcessors.put(
                getQualifiedClassName(java.lang.Math.class), javaMathClassMethodRelations);
        classProcessors.put(
                getQualifiedClassName(java.lang.StrictMath.class), javaMathClassMethodRelations);

        UnitsBoxedNumbersClassRelations javaNumberClassesMethodRelations =
                new UnitsBoxedNumbersClassRelations(checker, atypeFactory);
        classProcessors.put(
                getQualifiedClassName(java.lang.Number.class), javaNumberClassesMethodRelations);
        classProcessors.put(
                getQualifiedClassName(java.lang.Byte.class), javaNumberClassesMethodRelations);
        classProcessors.put(
                getQualifiedClassName(java.lang.Short.class), javaNumberClassesMethodRelations);
        classProcessors.put(
                getQualifiedClassName(java.lang.Integer.class), javaNumberClassesMethodRelations);
        classProcessors.put(
                getQualifiedClassName(java.lang.Long.class), javaNumberClassesMethodRelations);
        classProcessors.put(
                getQualifiedClassName(java.lang.Float.class), javaNumberClassesMethodRelations);
        classProcessors.put(
                getQualifiedClassName(java.lang.Double.class), javaNumberClassesMethodRelations);

        // Future TODO: add external class processors via reflection
    }

    /**
     * Returns an interned string of the canonical class name of clazz
     *
     * @param clazz the class whose name is to be retrieved
     * @return an interned string of the canonical class name
     */
    protected String getQualifiedClassName(Class<?> clazz) {
        return clazz.getCanonicalName().intern();
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, AnnotatedTypeMirror resultType) {
        String methodName = TreeUtils.methodName(node).toString().intern();
        AnnotatedTypeMirror receiver = factory.getReceiverType(node);

        if (receiver != null && receiver.getKind() == TypeKind.DECLARED) {
            AnnotatedDeclaredType receiverType = (AnnotatedDeclaredType) receiver;

            // stores the list of method call arguments
            List<? extends ExpressionTree> methodArguments = node.getArguments();

            // use the method receiver's class's fully qualified name as a
            // key to get the corresponding class processor
            // if that class has a processor, then invoke the processor to
            // process the invoked method
            // if there are no supporting class processors, then return
            // super.visitMethodInvocation()

            String methodClassQualifiedName =
                    TypesUtils.getQualifiedName(receiverType.getUnderlyingType())
                            .toString()
                            .intern();

            if (classProcessors.containsKey(methodClassQualifiedName)) {
                classProcessors
                        .get(methodClassQualifiedName)
                        .processMethodInvocation(methodName, methodArguments, resultType, node);
                // malformed implementations of a class processor by
                // external developers could remove all units annotations
                // from a result type, so assert that the result type has a
                // units annotation for soundness
                assert !resultType.getAnnotations().isEmpty();
                return null;
            }
        }

        return super.visitMethodInvocation(node, resultType);
    }

    @Override
    public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
        AnnotatedTypeMirror lht = factory.getAnnotatedType(node.getLeftOperand());
        AnnotatedTypeMirror rht = factory.getAnnotatedType(node.getRightOperand());
        Tree.Kind kind = node.getKind();

        factory.getUnitsMathOperatorsRelations().processMathOperation(node, kind, type, lht, rht);
        return null;
    }

    @Override
    public Void visitCompoundAssignment(
            CompoundAssignmentTree node, AnnotatedTypeMirror resultType) {
        // TODO: ATF Bug: this is only called for inner right hand
        // expressions of an assignment or compound assignment statement for
        // primitive types:
        // eg x = y -= z; (only called for the -=)
        // eg x += (y -= z); (only called for the -= but not the +=)
        //
        // Once this bug is fixed in ATF, the checking should only need to
        // occur in UnitsATF and not in UnitsVisitor

        AnnotatedTypeMirror varType = factory.getAnnotatedType(node.getVariable());
        AnnotatedTypeMirror exprType = factory.getAnnotatedType(node.getExpression());
        Tree.Kind kind = node.getKind();

        factory.getUnitsMathOperatorsRelations()
                .processCompoundAssignmentOperation(node, kind, resultType, varType, exprType);
        return null;
    }
}
