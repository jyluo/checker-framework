package org.checkerframework.checker.experimental.units_qual_poly;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.qualframework.base.QualifiedTypeMirror;
import org.checkerframework.qualframework.base.QualifiedTypeMirror.QualifiedDeclaredType;
import org.checkerframework.qualframework.base.QualifiedTypeMirror.QualifiedTypeVariable;
import org.checkerframework.qualframework.base.QualifiedTypeMirror.QualifiedWildcardType;
import org.checkerframework.qualframework.base.QualifierHierarchy;
import org.checkerframework.qualframework.base.SetQualifierVisitor;
import org.checkerframework.qualframework.base.TypeVariableSubstitutor;
import org.checkerframework.qualframework.base.dataflow.QualAnalysis;
import org.checkerframework.qualframework.base.dataflow.QualTransfer;
import org.checkerframework.qualframework.base.dataflow.QualValue;
import org.checkerframework.qualframework.poly.CombiningOperation;
import org.checkerframework.qualframework.poly.PolyQual;
import org.checkerframework.qualframework.poly.PolyQual.GroundQual;
import org.checkerframework.qualframework.poly.PolyQual.QualVar;
import org.checkerframework.qualframework.poly.QualParams;
import org.checkerframework.qualframework.poly.QualifiedParameterTypeVariableSubstitutor;
import org.checkerframework.qualframework.poly.QualifierParameterTreeAnnotator;
import org.checkerframework.qualframework.poly.QualifierParameterTypeFactory;
import org.checkerframework.qualframework.poly.SimpleQualifierParameterAnnotationConverter;
import org.checkerframework.qualframework.poly.Wildcard;
import org.checkerframework.qualframework.util.ExtendedTypeMirror;
import org.checkerframework.qualframework.util.QualifierContext;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The QualifiedTypeFactory for the Units-Qual-Param type system.
 *
 *
 */
public class UnitsQualifiedTypeFactory extends QualifierParameterTypeFactory<Units> {

    private final CombiningOperation<Units> lubOp = new CombiningOperation.Lub<>(new UnitsQualifierHierarchy());

    private final QualifierContext<QualParams<Units>> unitsChecker;
    protected final ProcessingEnvironment processingEnv;
    protected final Elements elements;

    private Map<String, UnitsQualifiedRelations> unitsRels; // Singleton instance

    private final Units TOP;
    private final Units MIXED;

    /**
     * The Pattern.compile method.
     *
     * @see Pattern#compile(String)
     */
    //    private final ExecutableElement patternCompile;

    public UnitsQualifiedTypeFactory(QualifierContext<QualParams<Units>> checker) {
        super(checker);
        unitsChecker = checker;
        processingEnv = unitsChecker.getProcessingEnvironment();
        elements = processingEnv.getElementUtils();

        // get a copy of the pattern compile method so the Regex checker can compile regex strings
        //        patternCompile = TreeUtils.getMethod("java.util.regex.Pattern", "compile",
        //                1, getContext().getProcessingEnvironment());

        // add default units relations class to units relations list
        this.getUnitsRels().put(UnitsQualifiedRelationsDefault.class.getCanonicalName(), 
                new UnitsQualifiedRelationsDefault().init(processingEnv));
        // TODO: add other units relations classes from user


        TOP = super.getQualifierHierarchy().getTop().getPrimary().getMaximum();
        MIXED = Units.MIXED;

        // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "TOP qual: " + TOP.toString());
        // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Units Qual Factory Created");
    }

    protected Map<String, UnitsQualifiedRelations> getUnitsRels()
    {
        if (unitsRels == null) {
            unitsRels = new HashMap<String, UnitsQualifiedRelations>();
        }
        return unitsRels;
    }

    // Ground Qualifier Hierarchy is the default type qualifier hierarchy that does not involve polymorphism
    @Override
    protected QualifierHierarchy<Units> createGroundQualifierHierarchy() {
        return new UnitsQualifierHierarchy(processingEnv);
    }

    @Override
    protected UnitsAnnotationConverter createAnnotationConverter() {
        return new UnitsAnnotationConverter(processingEnv);
    }

    @Override
    protected QualifierParameterTreeAnnotator<Units> createTreeAnnotator() {
        return new QualifierParameterTreeAnnotator<Units>(this) {

            // null Literal gets bottom
            @Override
            public QualifiedTypeMirror<QualParams<Units>> visitLiteral(LiteralTree tree, ExtendedTypeMirror type) {
                QualifiedTypeMirror<QualParams<Units>> result = super.visitLiteral(tree, type);

                if (tree.getKind() == Kind.NULL_LITERAL) {
                    return SetQualifierVisitor.apply(result, UnitsQualifiedTypeFactory.this.getQualifierHierarchy().getBottom());
                }

                return result;
            }



            /**
             * Create a Units qualifier based on the contents of string and char literals.
             * Null literals are Units.BOTTOM.
             */
            //            @Override
            //            public QualifiedTypeMirror<QualParams<Units>> visitLiteral(LiteralTree tree, ExtendedTypeMirror type) {
            //                QualifiedTypeMirror<QualParams<Units>> result = super.visitLiteral(tree, type);
            //
            //                if (tree.getKind() == Kind.NULL_LITERAL) {
            //                    return SetQualifierVisitor.apply(result, UnitsQualifiedTypeFactory.this.getQualifierHierarchy().getBottom());
            //                }
            //
            //                String UnitsStr = null;
            //                if (tree.getKind() == Kind.STRING_LITERAL) {
            //                    UnitsStr = (String) tree.getValue();
            //                } else if (tree.getKind() == Kind.CHAR_LITERAL) {
            //                    UnitsStr = Character.toString((Character) tree.getValue());
            //                }
            //
            //                if (UnitsStr != null) {
            //                    Units UnitsQual;
            //                    if (isUnits(UnitsStr)) {
            //                        int groupCount = getGroupCount(UnitsStr);
            //                        UnitsQual = new Units.UnitsVal(groupCount);
            //                    } else {
            //                        UnitsQual = new Units.PartialUnits(UnitsStr);
            //                    }
            //                    QualParams<Units> clone = result.getQualifier().clone();
            //                    clone.setPrimary(new GroundQual<>(UnitsQual));
            //                    result = SetQualifierVisitor.apply(result, clone);
            //                }
            //
            //                return result;
            //            }

            // Binary Operation

            @Override
            public QualifiedTypeMirror<QualParams<Units>> visitBinary(BinaryTree tree, ExtendedTypeMirror type) {
                QualifiedTypeMirror<QualParams<Units>> result = super.visitBinary(tree, type);

                // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "visiting binary tree Class: " + tree.getClass() + " Kind: " + tree.getKind());

                //                if (TreeUtils.isStringConcatenation(tree)
                //                        || (tree instanceof CompoundAssignmentTree
                //                                && TreeUtils.isStringCompoundConcatenation((CompoundAssignmentTree)tree))) {

                QualParams<Units> leftUnits = getEffectiveQualifier(getQualifiedType(tree.getLeftOperand()));
                QualParams<Units> rightUnits = getEffectiveQualifier(getQualifiedType(tree.getRightOperand()));

                PolyQual<Units> resultQual = null;
                PolyQual<Units> leftPrimary = leftUnits.getPrimary();
                PolyQual<Units> rightPrimary = rightUnits.getPrimary();

                Units bestUnit = null;
                Units leftUnit = leftPrimary.getMaximum();
                //                Units leftUnit = getQualifierHierarchy().getBottom() == leftUnits ?
                //                        new UnitsQualPool.getQualifier(String name, Prefix p, Units superUnit) : leftPrimary.getMaximum();
                //                
                Units rightUnit = rightPrimary.getMaximum();
                //              Units rightUnit = getQualifierHierarchy().getBottom() == rightUnits ?
                //                      new UnitsQualPool.getQualifier(String name, Prefix p, Units superUnit) : rightPrimary.getMaximum();
                //              

                if(leftUnit == null || rightUnit == null) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, " left Unit null: " + (leftUnit==null) + " right Unit null: " + (rightUnit ==null) );
                    return result;
                }

                PolyQual<Units> polyResult = checkPoly(leftPrimary, rightPrimary, leftUnit, rightUnit);
                if (polyResult != null) {
                    resultQual = polyResult;
                }

                if(leftUnit.equals(TOP) && rightUnit.equals(TOP)) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "both units are Unknown");
                    return result;
                }

                // Use Units Relations classes to figure out the return Unit
                for(UnitsQualifiedRelations unitRel : getUnitsRels().values()) {
                    Units unitRelResult = useUnitsRelation(tree.getKind(), unitRel, leftUnit, rightUnit);

                    //messager.printMessage(Kind.NOTE, " unit relations present " + (unitRel != null));
                    //messager.printMessage(Kind.NOTE, "best unit: " + bestUnit + " unit relations: " + unitRelResult);

                    // check to see if the two units match
                    if(bestUnit != null && unitRelResult != null && !bestUnit.equals(unitRelResult)) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                "UnitsRelation mismatch, taking neither! Previous: "
                                        + bestUnit + " and current: " + unitRelResult);
                        return result;  // super.visitBinary(node, type);
                    }

                    if(unitRelResult != null) {
                        // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "best unit found via unit relations");
                        bestUnit = unitRelResult;
                    }
                }

                // If Units Relations was unable to figure out the return Unit, decide based on standard rules
                if(bestUnit == null)
                {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "discovering best unit via default rules");
                    // detect tree kind, ie the operation
                    switch(tree.getKind()) {
                    case MINUS:
                        // same as plus
                    case PLUS:
                        if(leftUnit.equals(rightUnit)) {
                            bestUnit = leftUnit;
                        } else {
                            bestUnit = MIXED;
                        }
                        break;
                    case DIVIDE:
                        if(leftUnit.equals(rightUnit)) {
                            bestUnit = TOP;
                        }
                        else if(leftUnit != null && rightUnit.equals(TOP)) {
                            bestUnit = leftUnit;
                        }
                        else {
                            bestUnit = MIXED;
                        }
                        break;
                    case MULTIPLY:
                        if(leftUnit.equals(TOP) && rightUnit != null) {
                            bestUnit = rightUnit;
                        }
                        else if(rightUnit.equals(TOP) && leftUnit != null) {
                            bestUnit = leftUnit;
                        }
                        else {
                            bestUnit = MIXED;
                        }
                        break;
                    case REMAINDER:
                        bestUnit = leftUnit;
                        break;
                    default:
                        break;
                    }
                }

                if(bestUnit != null) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "returning best unit: " + bestUnit.toString());
                    // version 1
                    QualParams<Units> clone = result.getQualifier().clone();
                    clone.setPrimary(new GroundQual<>(bestUnit));
                    result = SetQualifierVisitor.apply(result, clone);
                    return result;

                    //                    // version 2:
                    //                    resultQual = new GroundQual<Units>(bestUnit);
                    //
                    //                    if (resultQual != null) {
                    //                        return new QualifiedDeclaredType<>(
                    //                                type, new QualParams<>(resultQual),
                    //                                new ArrayList<QualifiedTypeMirror<QualParams<Units>>>()
                    //                                );
                    //                    }
                }

                if (resultQual != null) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "returning poly unit: " + resultQual.toString());
                    return new QualifiedDeclaredType<>(
                            type, new QualParams<>(resultQual),
                            new ArrayList<QualifiedTypeMirror<QualParams<Units>>>()
                            );
                }

                return result;
            }

            @Override
            public QualifiedTypeMirror<QualParams<Units>> visitCompoundAssignment(CompoundAssignmentTree tree, ExtendedTypeMirror type) {
                QualifiedTypeMirror<QualParams<Units>> result = super.visitCompoundAssignment(tree, type);

                //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "visiting compound assign tree Class: " + tree.getClass() + " Kind: " + tree.getKind());

                QualParams<Units> leftUnits = getEffectiveQualifier(getQualifiedType(tree.getVariable()));
                QualParams<Units> rightUnits = getEffectiveQualifier(getQualifiedType(tree.getExpression()));

                PolyQual<Units> resultQual = null;
                PolyQual<Units> leftPrimary = leftUnits.getPrimary();
                PolyQual<Units> rightPrimary = rightUnits.getPrimary();

                Units bestUnit = null;
                Units leftUnit = leftPrimary.getMaximum();
                //                Units leftUnit = getQualifierHierarchy().getBottom() == leftUnits ?
                //                        new UnitsQualPool.getQualifier(String name, Prefix p, Units superUnit) : leftPrimary.getMaximum();
                //                
                Units rightUnit = rightPrimary.getMaximum();
                //              Units rightUnit = getQualifierHierarchy().getBottom() == rightUnits ?
                //                      new UnitsQualPool.getQualifier(String name, Prefix p, Units superUnit) : rightPrimary.getMaximum();
                //              

                if(leftUnit == null || rightUnit == null) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, " left Unit null: " + (leftUnit==null) + " right Unit null: " + (rightUnit ==null) );
                    return result;
                }

                PolyQual<Units> polyResult = checkPoly(leftPrimary, rightPrimary, leftUnit, rightUnit);
                if (polyResult != null) {
                    resultQual = polyResult;
                }

                if(leftUnit.equals(TOP) && rightUnit.equals(TOP)) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "both units are Unknown");
                    return result;
                }

                // Use Units Relations classes to figure out the return Unit
                for(UnitsQualifiedRelations unitRel : getUnitsRels().values()) {
                    Units unitRelResult = useUnitsRelation(tree.getKind(), unitRel, leftUnit, rightUnit);

                    //messager.printMessage(Kind.NOTE, " unit relations present " + (unitRel != null));
                    //messager.printMessage(Kind.NOTE, "best unit: " + bestUnit + " unit relations: " + unitRelResult);

                    // check to see if the two units match
                    if(bestUnit != null && unitRelResult != null && !bestUnit.equals(unitRelResult)) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                "UnitsRelation mismatch, taking neither! Previous: "
                                        + bestUnit + " and current: " + unitRelResult);
                        return result;  // super.visitBinary(node, type);
                    }

                    if(unitRelResult != null) {
                        // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "best unit found via unit relations");
                        bestUnit = unitRelResult;
                    }
                }

                // If Units Relations was unable to figure out the return Unit, decide based on standard rules
                if(bestUnit == null)
                {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "discovering best unit via default rules");
                    // detect tree kind, ie the operation
                    switch(tree.getKind()) {
                    case MINUS_ASSIGNMENT:
                        // same as plus
                    case PLUS_ASSIGNMENT:
                        if(leftUnit.equals(rightUnit)) {
                            bestUnit = leftUnit;
                        } else {
                            bestUnit = MIXED;
                        }
                        break;
                    case DIVIDE_ASSIGNMENT:
                        if(leftUnit != null && rightUnit.equals(TOP)) {
                            bestUnit = leftUnit;
                        }
                        else {
                            bestUnit = MIXED;
                        }
                        break;
                    case MULTIPLY_ASSIGNMENT:
                        if(leftUnit != null && rightUnit.equals(TOP)) {
                            bestUnit = leftUnit;
                        }
                        else {
                            bestUnit = MIXED;
                        }
                        break;
                    case REMAINDER_ASSIGNMENT:
                        bestUnit = leftUnit;
                        break;
                    default:
                        break;
                    }
                }

                if(bestUnit != null) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "returning best unit: " + bestUnit.toString());
                    // version 1
                    QualParams<Units> clone = result.getQualifier().clone();
                    clone.setPrimary(new GroundQual<>(bestUnit));
                    result = SetQualifierVisitor.apply(result, clone);
                    return result;

                    //                    // version 2:
                    //                    resultQual = new GroundQual<Units>(bestUnit);
                    //
                    //                    if (resultQual != null) {
                    //                        return new QualifiedDeclaredType<>(
                    //                                type, new QualParams<>(resultQual),
                    //                                new ArrayList<QualifiedTypeMirror<QualParams<Units>>>()
                    //                                );
                    //                    }
                }

                if (resultQual != null) {
                    // processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "returning poly unit: " + resultQual.toString());
                    return new QualifiedDeclaredType<>(
                            type, new QualParams<>(resultQual),
                            new ArrayList<QualifiedTypeMirror<QualParams<Units>>>()
                            );
                }

                return result;
            }




            /**
             * Handle string compound assignment.
             */
            //            @Override
            //            public QualifiedTypeMirror<QualParams<Units>> visitCompoundAssignment(CompoundAssignmentTree tree,
            //                    ExtendedTypeMirror type) {
            //
            //                if (TreeUtils.isStringConcatenation(tree) || TreeUtils.isStringCompoundConcatenation(tree)) {
            //
            //                    QualParams<Units> lUnits = getEffectiveQualifier(getQualifiedType(tree.getExpression()));
            //                    QualParams<Units> rUnits = getEffectiveQualifier(getQualifiedType(tree.getVariable()));
            //                    QualifiedTypeMirror<QualParams<Units>> result =
            //                            handleBinaryOperation(tree, lUnits, rUnits, type);
            //
            //                    if (result != null) {
            //                        return result;
            //                    }
            //                }
            //                return super.visitCompoundAssignment(tree, type);
            //            }

            /**
             * Add polymorphism to the Pattern.compile and Pattern.matcher methods.
             */
            //            @Override
            //            public QualifiedTypeMirror<QualParams<Units>> visitMethodInvocation(MethodInvocationTree tree, ExtendedTypeMirror type) {
            //                // TODO: Also get this to work with 2 argument Pattern.compile.
            //
            //                QualifiedTypeMirror<QualParams<Units>> result = super.visitMethodInvocation(tree, type);
            //
            //                if (TreeUtils.isMethodInvocation(tree, patternCompile,
            //                        getContext().getProcessingEnvironment())) {
            //
            //                    ExpressionTree arg0 = tree.getArguments().get(0);
            //                    if (getEffectiveQualifier(getQualifiedType(arg0)) == UnitsQualifiedTypeFactory.this.getQualifierHierarchy().getBottom()) {
            //                        result = SetQualifierVisitor.apply(result, UnitsQualifiedTypeFactory.this.getQualifierHierarchy().getBottom());
            //                    } else {
            //                        Units qual = getEffectiveQualifier(getQualifiedType(arg0)).getPrimary().getMaximum();
            //                        QualParams<Units> clone = result.getQualifier().clone();
            //                        clone.setPrimary(new GroundQual<>(qual));
            //                        result = SetQualifierVisitor.apply(result, clone);
            //                    }
            //                }
            //                return result;
            //            }

            /**
             * Handle concatenation of Units or PolyUnits String/char literals.
             * Also handles concatenation of partial regular expressions.
             */
            //            @Override
            //            public QualifiedTypeMirror<QualParams<Units>> visitBinary(BinaryTree tree, ExtendedTypeMirror type) {
            //
            //                if (TreeUtils.isStringConcatenation(tree)
            //                        || (tree instanceof CompoundAssignmentTree
            //                        && TreeUtils.isStringCompoundConcatenation((CompoundAssignmentTree)tree))) {
            //
            //                    QualParams<Units> lUnits = getEffectiveQualifier(getQualifiedType(tree.getLeftOperand()));
            //                    QualParams<Units> rUnits = getEffectiveQualifier(getQualifiedType(tree.getRightOperand()));
            //                    QualifiedTypeMirror<QualParams<Units>> result =
            //                            handleBinaryOperation(tree, lUnits, rUnits, type);
            //                    if (result != null) {
            //                        return result;
            //                    }
            //                }
            //                return super.visitBinary(tree, type);
            //            }

            /**
             * Returns the QualifiedTypeMirror that is the result of the binary operation represented by tree.
             * Handles concatenation of Units and PolyUnits qualifiers.
             *
             * @param tree A BinaryTree or a CompoundAssignmentTree
             * @param lUnitsParam The qualifier of the left hand side of the expression
             * @param rUnitsParam The qualifier of the right hand side of the expression
             * @return result if operation is not a string concatenation or compound assignment. Otherwise
             *          a copy of result with the new qualifier applied is returned.
             */
            //            private QualifiedTypeMirror<QualParams<Units>> handleBinaryOperation(Tree tree, QualParams<Units> lUnitsParam,
            //                    QualParams<Units> rUnitsParam, ExtendedTypeMirror type) {
            //
            //                if (TreeUtils.isStringConcatenation(tree)
            //                        || (tree instanceof CompoundAssignmentTree
            //                            && TreeUtils.isStringCompoundConcatenation((CompoundAssignmentTree)tree))) {
            //
            //                    PolyQual<Units> resultQual = null;
            //
            //                    PolyQual<Units> rPrimary = rUnitsParam.getPrimary();
            //                    PolyQual<Units> lPrimary = lUnitsParam.getPrimary();
            //
            //                    Units rUnits = getQualifierHierarchy().getBottom() == rUnitsParam ?
            //                            new Units.UnitsVal(0) : rPrimary.getMaximum();
            //                    Units lUnits = getQualifierHierarchy().getBottom() == lUnitsParam ?
            //                            new Units.UnitsVal(0) : lPrimary.getMaximum();
            //
            //                    PolyQual<Units> polyResult = checkPoly(lPrimary, rPrimary, lUnits, rUnits);
            //                    if (polyResult != null) {
            //                        resultQual = polyResult;
            //
            //                    } else if (lUnits.isUnitsVal() && rUnits.isUnitsVal()) {
            //                        // Units(a) + Units(b) = Units(a + b)
            //                        int resultCount = ((Units.UnitsVal) lUnits).getCount() + ((Units.UnitsVal) rUnits).getCount();
            //                        resultQual = new GroundQual<Units>(new Units.UnitsVal(resultCount));
            //
            //                    } else if (lUnits.isPartialUnits() && rUnits.isPartialUnits()) {
            //                        // Partial + Partial == Units or Partial
            //                        String concat = ((Units.PartialUnits) lUnits).getPartialValue() + ((Units.PartialUnits) rUnits).getPartialValue();
            //                        if (isUnits(concat)) {
            //                            int groupCount = getGroupCount(concat);
            //                            resultQual = new GroundQual<Units>(new Units.UnitsVal(groupCount));
            //                        } else {
            //                            resultQual = new GroundQual<Units>(new Units.PartialUnits(concat));
            //                        }
            //
            //                    } else if (lUnits.isUnitsVal() && rUnits.isPartialUnits()) {
            //                        // Units + Partial == Partial
            //                        String concat = "e" + ((Units.PartialUnits) rUnits).getPartialValue();
            //                        resultQual = new GroundQual<Units>(new Units.PartialUnits(concat));
            //
            //                    } else if (rUnits.isUnitsVal() && lUnits.isPartialUnits()) {
            //                        // Partial + Units == Partial
            //                        String concat = ((Units.PartialUnits) lUnits).getPartialValue() + "e";
            //                        resultQual = new GroundQual<Units>(new Units.PartialUnits(concat));
            //                    } else if (rUnits == Units.TOP || lUnits == Units.TOP) {
            //                        resultQual = new GroundQual<>(Units.TOP);
            //                    } else if (rUnits == Units.BOTTOM && lUnits == Units.BOTTOM) {
            //                        resultQual = new GroundQual<>(Units.BOTTOM);
            //                    }
            //
            //                    if (resultQual != null) {
            //                        return new QualifiedDeclaredType<>(type, new QualParams<>(resultQual),
            //                                new ArrayList<QualifiedTypeMirror<QualParams<Units>>>());
            //                    }
            //                }
            //
            //                return null;
            //            }

        }; // End of return new QualifierParameterTreeAnnotator<Units>(this)
    }

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

    /**
     * Check to see if the result of the operation is polymorphic.
     *
     * @return the polymorphic PolyQual if the result should be polymorphic, otherwise return null.
     */
    private PolyQual<Units> checkPoly(PolyQual<Units> leftPrimary, PolyQual<Units> rightPrimary, Units leftUnits, Units rightUnits) {
        if(isPolyUnits(leftPrimary)) {
            return leftPrimary;
        }
        else if(isPolyUnits(rightPrimary)) {
            return rightPrimary;
        }
        else {
            return null;
        }

        //        if (isPolyUnits(leftPrimary) && isPolyUnits(rightPrimary)) {
        //            return leftPrimary;
        //        } else if (isPolyUnits(leftPrimary) && rightUnits.isUnitsVal()) {
        //            return leftPrimary;
        //        } else if (isPolyUnits(rightPrimary) && leftUnits.isUnitsVal()) {
        //            return rightPrimary;
        //        } else {
        //            return null;
        //        }
    }

    // checks to see if the qualifier is a polymorphic qualifier
    private boolean isPolyUnits(PolyQual<Units> possiblePoly) {
        // it is a poly qualifier if it is a qual variable and

        return (possiblePoly instanceof QualVar)
                // it's name is the same as "_poly", or whichever is hard coded into the simple qual param annotation converter
                && ((QualVar<?>) possiblePoly).getName().equals(SimpleQualifierParameterAnnotationConverter.POLY_NAME);
    }

    // add flow analysis
    @Override
    public QualAnalysis<QualParams<Units>> createFlowAnalysis(List<Pair<VariableElement, QualValue<QualParams<Units>>>> fieldValues) {
        return new QualAnalysis<QualParams<Units>>(this.getContext()) {
            @Override
            public QualTransfer<QualParams<Units>> createTransferFunction() {
                return new UnitsQualifiedTransfer(this);
            }
        };
    }

    // type substitution??
    @Override
    public TypeVariableSubstitutor<QualParams<Units>> createTypeVariableSubstitutor() {
        return new QualifiedParameterTypeVariableSubstitutor<Units>() {
            @Override
            protected Wildcard<Units> combineForSubstitution(Wildcard<Units> a, Wildcard<Units> b) {
                return a.combineWith(b, lubOp, lubOp);
            }

            @Override
            protected PolyQual<Units> combineForSubstitution(PolyQual<Units> a, PolyQual<Units> b) {
                return a.combineWith(b, lubOp);
            }
        };
    }

    // 
    public QualParams<Units> getEffectiveQualifier(QualifiedTypeMirror<QualParams<Units>> mirror) {

        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Kind: " + mirror.getKind() + " val: " + mirror.toString());

        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "        " + mirror.getQualifier() );        // Units qual

        switch (mirror.getKind()) {
        case TYPEVAR:
            return this.getQualifiedTypeParameterBounds(
                    ((QualifiedTypeVariable<QualParams<Units>>) mirror).
                    getDeclaration().getUnderlyingType()).getUpperBound().getQualifier();
        case WILDCARD:
            return ((QualifiedWildcardType<QualParams<Units>>)mirror).getExtendsBound().getQualifier();

        default:
            return mirror.getQualifier();
        }
    }
}
