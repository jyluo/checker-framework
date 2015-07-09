package org.checkerframework.checker.experimental.units_qual;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;

import org.checkerframework.checker.experimental.regex_qual.Regex;
import org.checkerframework.checker.experimental.regex_qual.RegexQualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.qualframework.base.AnnotationConverter;
import org.checkerframework.qualframework.base.DefaultQualifiedTypeFactory;
import org.checkerframework.qualframework.base.QualifiedTypeMirror;
import org.checkerframework.qualframework.base.QualifierHierarchy;
import org.checkerframework.qualframework.base.SetQualifierVisitor;
import org.checkerframework.qualframework.base.TreeAnnotator;
import org.checkerframework.qualframework.base.QualifiedTypeMirror.QualifiedTypeVariable;
import org.checkerframework.qualframework.base.QualifiedTypeMirror.QualifiedWildcardType;
import org.checkerframework.qualframework.base.dataflow.QualAnalysis;
import org.checkerframework.qualframework.base.dataflow.QualTransfer;
import org.checkerframework.qualframework.base.dataflow.QualValue;
import org.checkerframework.qualframework.util.ExtendedTypeMirror;
import org.checkerframework.qualframework.util.QualifierContext;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic.Kind;

/**
 * The QualifiedTypeFactory for the Units-Qual type system.
 *
 * Note: Polymorphic qualifiers are not supported automatically by the qual system.
 * Instead, only the most basic and required polymorphic methods are manually
 * supported by visitMethodInvocation.
 *
 * @see org.checkerframework.checker.units.UnitsAnnotatedTypeFactory
 *
 */

public class UnitsQualifiedTypeFactory extends DefaultQualifiedTypeFactory<Units> {

    /** The checker to use for option handling and resource management. */
    protected final QualifierContext<Units> checker;

    /** The processing environment to use for accessing compiler internals. */
    protected final ProcessingEnvironment processingEnv;

    /** The messenger used to print out warnings and errors */
    protected final Messager messager;

    /** Utility class for working with {@link Element}s. */
    protected final Elements elements;

    //protected final AnnotationMirror mixedUnits;
    //protected AnnotationMirror BOTTOM;

    private Map<String, UnitsQualifiedRelations> unitsRels; // Singleton instance

    //private final ExecutableElement unitExecElement;

    public UnitsQualifiedTypeFactory(QualifierContext<Units> checker) {
        super(checker);

        this.processingEnv = checker.getProcessingEnvironment();
        this.messager = processingEnv.getMessager();

        this.checker = checker;
        this.elements = processingEnv.getElementUtils();

        //mixedUnits = AnnotationUtils.fromClass(elements, MixedUnits.class);

        //BOTTOM = AnnotationUtils.fromClass(elements, UnitsBottom.class);

        /*
        this.postInit();

        addTypeNameImplicit(java.lang.Void.class, BOTTOM);
         */

        /** Add the default unit relations to the set of units relations */
        // TODO: move this to some kind of Create Supported Type Qualifiers method?
        // TODO: check to see if canonical name works? if not change to
        // "org.checkerframework.checker.units.UnitsRelationsDefault" etc

        this.getUnitsRels().put(UnitsQualifiedRelationsDefault.class.getCanonicalName(), 
                new UnitsQualifiedRelationsDefault().init(processingEnv));

        /*
          // TODO: Try catch?
          Class<UnitsQualifiedRelationsDefault> theclass = UnitsQualifiedRelationsDefault.class;
          try {
              unitsRels.put(theclass.getName(), ((UnitsQualifiedRelations) theclass.newInstance()).init(processingEnv));
          } catch (InstantiationException e) {
              e.printStackTrace();      // what kind of stack trace?
          } catch (IllegalAccessException e) {
              e.printStackTrace();
          }
         */

    }

    protected Map<String, UnitsQualifiedRelations> getUnitsRels()
    {
        if (unitsRels == null) {
            unitsRels = new HashMap<String, UnitsQualifiedRelations>();
        }
        return unitsRels;
    }

    @Override
    protected QualifierHierarchy<Units> createQualifierHierarchy() {
        return new UnitsQualifierHierarchy(this.processingEnv);
        //return new UnitsQualifierHierarchy();
    }

    @Override
    protected AnnotationConverter<Units> createAnnotationConverter() {
        return new UnitsAnnotationConverter(this.processingEnv);
        // return new UnitsAnnotationConverter();
    }

    @Override
    protected TreeAnnotator<Units> createTreeAnnotator() {
        return new TreeAnnotator<Units>() {

            /* Rules:
             * 1) Null literals are considered BOTTOM
             * 2) Propagation Tree Annotator
             *       // does nothing???
             * 3) Tree Annotator
             *       visit binary
             *       visit compound assignment
             *       
             *       qualifierhierarhcy...?
             */


            @Override
            public QualifiedTypeMirror<Units> visitAssignment(AssignmentTree tree, ExtendedTypeMirror type) {
                QualifiedTypeMirror<Units> result = super.visitAssignment(tree, type);

                messager.printMessage(Kind.NOTE, "visit Assignment " + tree.getVariable() + " = " + tree.getExpression());

                if(tree.getKind() == Tree.Kind.ASSIGNMENT) {

                }

                return result;
            }

            @Override
            public QualifiedTypeMirror<Units> visitExpressionStatement(ExpressionStatementTree tree, ExtendedTypeMirror type) {

                QualifiedTypeMirror<Units> result = super.visitExpressionStatement(tree, type);
                messager.printMessage(Kind.NOTE, "visit Expression Stmt " + tree.getExpression());


                return result;
            }


            @Override
            public QualifiedTypeMirror<Units> visitMethodInvocation(MethodInvocationTree tree, ExtendedTypeMirror type) {

                QualifiedTypeMirror<Units> result = super.visitMethodInvocation(tree, type);
                //messager.printMessage(Kind.NOTE, "visit Method Inovcation " + tree.getMethodSelect());


                return result;

            }

            /**
             * Create a Units qualifier based on the contents of _____
             * ____ are Units.BOTTOM.
             */
            @Override
            public QualifiedTypeMirror<Units> visitLiteral(LiteralTree tree, ExtendedTypeMirror type) {
                QualifiedTypeMirror<Units> result = super.visitLiteral(tree, type);

                // messager.printMessage(Kind.NOTE, "visitLiteral - value: " + tree.getValue());

                /** null literals are considered BOTTOM */
                if(tree.getKind() == Tree.Kind.NULL_LITERAL) {
                    return SetQualifierVisitor.apply(result, Units.BOTTOM);
                }
                return result;
            }

            /**
             * Binary operation is any kind of operation with a left and right side
             * 
             * Handle concatenation of Regex or PolyRegex String/char literals.
             * Also handles concatenation of partial regular expressions.
             */
            @Override
            public QualifiedTypeMirror<Units> visitBinary(BinaryTree tree, ExtendedTypeMirror type) {

                QualifiedTypeMirror<Units> result = super.visitBinary(tree, type);

                Units leftUnit = getEffectiveQualifier(getQualifiedType(tree.getLeftOperand()));
                Units rightUnit = getEffectiveQualifier(getQualifiedType(tree.getRightOperand()));
                Tree.Kind kind = tree.getKind();

                // messager.printMessage(Kind.NOTE, "visitBinary - kind: " + kind.toString() + " left Unit: " + leftUnit + " right Unit: " + rightUnit);

                //AnnotationMirror bestResult = null;
                Units bestUnit = null;

                // if either cannot retrieve a unit, we have a failure
                // 
                if(leftUnit == null || rightUnit == null) {
                    // TODO: raise some exception
                    return result;
                }

                // if both do not have units, then leave it be, return the result
                if(leftUnit.equals(Units.UnitsUnknown) && rightUnit.equals(Units.UnitsUnknown)) {
                    return result;
                }

                // check to see if units relations can determine what type it is
                // if so, use that type
                for(UnitsQualifiedRelations unitRel : getUnitsRels().values()) {
                    Units unitRelResult = useUnitsRelation(kind, unitRel, leftUnit, rightUnit);

                    //messager.printMessage(Kind.NOTE, " unit relations present " + (unitRel != null));
                    //messager.printMessage(Kind.NOTE, "best unit: " + bestUnit + " unit relations: " + unitRelResult);

                    // check to see if the two units match
                    if(bestUnit != null && unitRelResult != null && !bestUnit.equals(unitRelResult)) {
                        messager.printMessage(Kind.WARNING,
                                "UnitsRelation mismatch, taking neither! Previous: "
                                        + bestUnit + " and current: " + unitRelResult);
                        return null;  // super.visitBinary(node, type);
                    }

                    if(unitRelResult != null) {
                        bestUnit = unitRelResult;
                    }
                }

                // if units qualified relations didn't produce a unit, then assign something 
                // according to the following algorithm
                if(bestUnit == null) {
                    switch (kind)  {
                    case MINUS:
                        // same as plus
                    case PLUS:
                        // if left hand annotation equals right hand annotation, add the annotation to the result type
                        // both must be the same
                        if(leftUnit.equals(rightUnit))
                        {
                            bestUnit = leftUnit;
                        }
                        // else add mixedUnits to the result type
                        else
                        {
                            bestUnit = Units.MIXED;
                        }
                        break;
                    case DIVIDE:
                        //messager.printMessage(Kind.NOTE, "visitBinary - div - left Unit: " + leftUnit + " right Unit: " + rightUnit);

                        // if the units of the division match set the unit to UnitsUnknown
                        if(leftUnit.equals(rightUnit))
                        {
                            bestUnit = Units.UnitsUnknown;
                        }
                        // if the left has a unit and the right is a unit-less constant return the left unit
                        else if(leftUnit != null && rightUnit.equals(Units.UnitsUnknown) )
                        {
                            bestUnit = leftUnit;
                        }
                        // else add mixedUnits to the result type (eg for s / m)
                        else
                        {
                            bestUnit = Units.MIXED;
                        }
                        break;
                    case MULTIPLY:
                        //messager.printMessage(Kind.NOTE, "visitBinary - mul - left Unit: " + leftUnit + " right Unit: " + rightUnit);

                        // if left has no units, then transfer the annotation from the right
                        if(leftUnit.equals(Units.UnitsUnknown) && rightUnit != null)
                        {
                            bestUnit = rightUnit;
                        }
                        // if right has no units, then transfer the annotation from the left
                        else if(rightUnit.equals(Units.UnitsUnknown) && leftUnit != null)
                        {
                            bestUnit = leftUnit;
                        }
                        else
                        {
                            // if both have units (and UnitsRelations didn't provide a resulting Unit)
                            //messager.printMessage(Kind.NOTE, "visitBinary mult - left Unit: " + leftUnit + " right Unit: " + rightUnit);
                            bestUnit = Units.MIXED;
                        }

                        break;
                    case REMAINDER:     // this is for % operation
                        // modulus equivalence equations
                        // r = n - (n / b) * b; assuming integer division
                        // n = (n / b) * b + (n % b);

                        // n => no unit, b => has unit: has cases such as 1 Hz === (no unit or count) / second
                        // n => has unit, b => has unit: has cases such as meter % meter
                        // n => has unit, b => no unit: has cases such as meter % constant

                        //if(leftUnit != null)
                        bestUnit = leftUnit;

                        break;
                    default:
                        // Do nothing
                        break;
                    }
                }

                if(bestUnit != null)
                {
                    return SetQualifierVisitor.apply(result, bestUnit);
                }
                else
                {
                    return result;      // return the result from super
                }

            }



            /**
             * Handle compound assignment (eg: +=, -=, ...)
             */
            @Override
            public QualifiedTypeMirror<Units> visitCompoundAssignment(CompoundAssignmentTree tree,
                    ExtendedTypeMirror type) {

                QualifiedTypeMirror<Units> result = super.visitCompoundAssignment(tree, type);

                // statement format for compound assignment:
                // variable operator expression
                Units leftUnit = getEffectiveQualifier(getQualifiedType(tree.getVariable()));
                Units rightUnit = getEffectiveQualifier(getQualifiedType(tree.getExpression()));
                Tree.Kind kind = tree.getKind();

                // messager.printMessage(Kind.NOTE, "visitCompoundAssignment - kind: " + kind.toString() + " left Unit: " + leftUnit + " right Unit: " + rightUnit);

                Units bestUnit = null;

                // if neither has units then leave it be
                if(leftUnit == null || rightUnit == null)
                {
                    // TODO: raise some exception
                    return result;
                }

                // if both do not have units, then leave it be, return the result
                if(leftUnit.equals(Units.UnitsUnknown) && rightUnit.equals(Units.UnitsUnknown)) {
                    return result;
                }
                
                /*
                // For now, compound assignments are not checked via Unit Relations
                // TODO: add support
                 * 
                // check to see if units relations can determine what type it is
                // if so, use that type
                for(UnitsQualifiedRelations unitRel : unitsRels.values()) {
                    Units unitRelResult = useUnitsRelation(kind, unitRel, leftUnit, rightUnit);

                    // check to see if the two units match
                    if(bestUnit != null && unitRelResult != null && !bestUnit.equals(unitRelResult)) {
                        messager.printMessage(Kind.WARNING,
                                "UnitsRelation mismatch, taking neither! Previous: "
                                        + bestUnit + " and current: " + unitRelResult);
                        return null;  // super.visitBinary(node, type);
                    }

                    if(unitRelResult != null) {
                        bestUnit = unitRelResult;
                    }
                }
                 */

                // if units qualified relations didn't produce a unit, then assign something 
                // according to the following algorithm
                if(bestUnit == null) {
                    switch (kind)  {
                    case MINUS_ASSIGNMENT:  // -=
                        // same as plus
                    case PLUS_ASSIGNMENT:   // +=
                        // if both have the same units
                        if(leftUnit.equals(rightUnit)) {
                            bestUnit = leftUnit;
                        } 
                        // if only one of the two has a unit
                        else {
                            bestUnit = Units.MIXED;
                        }
                        break;
                    case DIVIDE_ASSIGNMENT: // /=
                        // messager.printMessage(Kind.NOTE, " div assignment " + leftUnit + " " + rightUnit);
                        // only left has units
                        if(leftUnit != null && rightUnit.equals(Units.UnitsUnknown)) {
                            // return left unit
                            bestUnit = leftUnit;
                        }
                        // only right has units
                        else {
                            bestUnit = Units.MIXED;
                        }
                        break;
                    case MULTIPLY_ASSIGNMENT: // *=
                        // messager.printMessage(Kind.NOTE, " mul assignment " + leftUnit + " " + rightUnit);
                        // only left has units
                        if(leftUnit != null && rightUnit.equals(Units.UnitsUnknown)) {
                            // return left unit
                            bestUnit = leftUnit;
                        }
                        // only right has units
                        else {
                            bestUnit = Units.MIXED;
                        }
                        break;
                    case REMAINDER_ASSIGNMENT: // %=
                        bestUnit = leftUnit;
                        break;

                        /*
                        // Boolean compound assignment operators
                        case AND_ASSIGNMENT:    // &=
                        case OR_ASSIGNMENT:     // |=
                        case XOR_ASSIGNMENT:    // ^=
                        // Bitwise compound assignment operators
                        case LEFT_SHIFT_ASSIGNMENT:     // <<=
                        case RIGHT_SHIFT_ASSIGNMENT:    // >>=
                        case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:   // >>>=
                         */
                    default:
                        // Do nothing
                        break;
                    }
                }

                if(bestUnit != null)
                {
                    return SetQualifierVisitor.apply(result, bestUnit);
                }
                else
                {
                    return result;      // return the result from super
                }
            }


        }; // end new TreeAnnotator<Units>
    }


    //TODO: dataflow analysis?


    /**
     * Configure dataflow to use the UnitsQualifiedTransfer.
     */
    @Override
    public QualAnalysis<Units> createFlowAnalysis(List<Pair<VariableElement, QualValue<Units>>> fieldValues) {
        return new QualAnalysis<Units>(this.getContext()) {
            @Override
            public QualTransfer<Units> createTransferFunction() {
                return new UnitsQualifiedTransfer(this);
            }
        };
    }

    //TODO: add support for custom qualified unit relations

    /**
     * Look for an @UnitsQualifiedRelations annotation on the qualifier and
     * add it to the list of UnitsQualifiedRelations.
     *
     * @param qual The qualifier to investigate.
     */
    private void addUnitsRelations(Class<? extends Annotation> qual) {
        AnnotationMirror am = AnnotationUtils.fromClass(elements, qual);

        for (AnnotationMirror ama : am.getAnnotationType().asElement().getAnnotationMirrors() ) {
            if (ama.getAnnotationType().toString().equals(UnitsQualifiedRelations.class.getCanonicalName())) {
                @SuppressWarnings("unchecked")
                Class<? extends UnitsQualifiedRelations> theclass = (Class<? extends UnitsQualifiedRelations>)
                AnnotationUtils.getElementValueClass(ama, "value", true);
                String classname = theclass.getCanonicalName();

                if (!getUnitsRels().containsKey(classname)) {
                    try {
                        unitsRels.put(classname, ((UnitsQualifiedRelations) theclass.newInstance()).init(processingEnv));
                    } catch (InstantiationException e) {
                        // TODO
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO
                        e.printStackTrace();
                    }
                }
            }
        }

        return;
    }

    /**
     * Uses the Units Relations class to figure out the unit to assign as the result of a multiplication or division
     * input : kind of tree, either division or multiplication
     * ur : instance of UnitsRelations class
     * leftUnit : the Unit of the left hand operand
     * rightUnit : the Unit of the right hand operand
     * return : a Unit for the result of the operation
     */
    private Units useUnitsRelation(Tree.Kind kind, UnitsQualifiedRelations ur, Units leftUnit, Units rightUnit)
    {
        Units result = null;

        if(ur != null)
        {
            //messager.printMessage(Kind.NOTE, "units relations " + ur.getClass().toString());
            switch(kind)
            {
            case DIVIDE:
                result = ur.division(leftUnit, rightUnit);
                break;
            case MULTIPLY:
                result = ur.multiplication(leftUnit, rightUnit);
                break;
                //TODO: add in modulus, compound assignment

            default:
                // Do nothing
            }
        }
        return result;
    }

    // TODO: use old implementation from Regex

    public Units getEffectiveQualifier(QualifiedTypeMirror<Units> mirror) {
        //messager.printMessage(Kind.NOTE, " get effective qual - mirror kind : " + mirror.getKind());

        // default
        Units result = mirror.getQualifier();

        switch (mirror.getKind()) {
        case TYPEVAR:
            result = this.getQualifiedTypeParameterBounds(
                    ((QualifiedTypeVariable<Units>) mirror).
                    getDeclaration().getUnderlyingType()).getUpperBound().getQualifier(); 
            break;
        case WILDCARD:
            result = ((QualifiedWildcardType<Units>)mirror).getExtendsBound().getQualifier();
            break;
        default:
            break;
        }

        //messager.printMessage(Kind.NOTE, " get effective qual - result : " + result);

        return result;
    }

}

