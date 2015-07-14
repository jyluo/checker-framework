package org.checkerframework.checker.experimental.units_qual_poly;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;

import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;
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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Units.Pattern;
import java.util.Units.PatternSyntaxException;

/**
 * The QualifiedTypeFactory for the Units-Qual-Param type system.
 *
 *
 */
public class UnitsQualifiedTypeFactory extends QualifierParameterTypeFactory<Units> {

    private final CombiningOperation<Units> lubOp = new CombiningOperation.Lub<>(new UnitsQualifierHierarchy());

    /**
     * The Pattern.compile method.
     *
     * @see Pattern#compile(String)
     */
    private final ExecutableElement patternCompile;

    public UnitsQualifiedTypeFactory(QualifierContext<QualParams<Units>> checker) {
        super(checker);

        // get a copy of the pattern compile method so the Regex checker can compile regex strings
        patternCompile = TreeUtils.getMethod("java.util.Units.Pattern", "compile",
                1, getContext().getProcessingEnvironment());
    }

    // Ground Qualifier Hierarchy is the default type qualifier hierarchy that does not involve polymorphism
    @Override
    protected QualifierHierarchy<Units> createGroundQualifierHierarchy() {
        return new UnitsQualifierHierarchy();
    }

    @Override
    protected UnitsAnnotationConverter createAnnotationConverter() {
        return new UnitsAnnotationConverter();
    }

    @Override
    protected QualifierParameterTreeAnnotator<Units> createTreeAnnotator() {
        return new QualifierParameterTreeAnnotator<Units>(this) {

            /**
             * Create a Units qualifier based on the contents of string and char literals.
             * Null literals are Units.BOTTOM.
             */
            @Override
            public QualifiedTypeMirror<QualParams<Units>> visitLiteral(LiteralTree tree, ExtendedTypeMirror type) {
                QualifiedTypeMirror<QualParams<Units>> result = super.visitLiteral(tree, type);

                if (tree.getKind() == Kind.NULL_LITERAL) {
                    return SetQualifierVisitor.apply(result, UnitsQualifiedTypeFactory.this.getQualifierHierarchy().getBottom());
                }

                String UnitsStr = null;
                if (tree.getKind() == Kind.STRING_LITERAL) {
                    UnitsStr = (String) tree.getValue();
                } else if (tree.getKind() == Kind.CHAR_LITERAL) {
                    UnitsStr = Character.toString((Character) tree.getValue());
                }

                if (UnitsStr != null) {
                    Units UnitsQual;
                    if (isUnits(UnitsStr)) {
                        int groupCount = getGroupCount(UnitsStr);
                        UnitsQual = new Units.UnitsVal(groupCount);
                    } else {
                        UnitsQual = new Units.PartialUnits(UnitsStr);
                    }
                    QualParams<Units> clone = result.getQualifier().clone();
                    clone.setPrimary(new GroundQual<>(UnitsQual));
                    result = SetQualifierVisitor.apply(result, clone);
                }

                return result;
            }

            /**
             * Handle string compound assignment.
             */
            @Override
            public QualifiedTypeMirror<QualParams<Units>> visitCompoundAssignment(CompoundAssignmentTree tree,
                    ExtendedTypeMirror type) {

                if (TreeUtils.isStringConcatenation(tree) || TreeUtils.isStringCompoundConcatenation(tree)) {

                    QualParams<Units> lUnits = getEffectiveQualifier(getQualifiedType(tree.getExpression()));
                    QualParams<Units> rUnits = getEffectiveQualifier(getQualifiedType(tree.getVariable()));
                    QualifiedTypeMirror<QualParams<Units>> result =
                            handleBinaryOperation(tree, lUnits, rUnits, type);

                    if (result != null) {
                        return result;
                    }
                }
                return super.visitCompoundAssignment(tree, type);
            }

            /**
             * Add polymorphism to the Pattern.compile and Pattern.matcher methods.
             */
            @Override
            public QualifiedTypeMirror<QualParams<Units>> visitMethodInvocation(MethodInvocationTree tree, ExtendedTypeMirror type) {
                // TODO: Also get this to work with 2 argument Pattern.compile.

                QualifiedTypeMirror<QualParams<Units>> result = super.visitMethodInvocation(tree, type);

                if (TreeUtils.isMethodInvocation(tree, patternCompile,
                        getContext().getProcessingEnvironment())) {

                    ExpressionTree arg0 = tree.getArguments().get(0);
                    if (getEffectiveQualifier(getQualifiedType(arg0)) == UnitsQualifiedTypeFactory.this.getQualifierHierarchy().getBottom()) {
                        result = SetQualifierVisitor.apply(result, UnitsQualifiedTypeFactory.this.getQualifierHierarchy().getBottom());
                    } else {
                        Units qual = getEffectiveQualifier(getQualifiedType(arg0)).getPrimary().getMaximum();
                        QualParams<Units> clone = result.getQualifier().clone();
                        clone.setPrimary(new GroundQual<>(qual));
                        result = SetQualifierVisitor.apply(result, clone);
                    }
                }
                return result;
            }

            /**
             * Handle concatenation of Units or PolyUnits String/char literals.
             * Also handles concatenation of partial regular expressions.
             */
            @Override
            public QualifiedTypeMirror<QualParams<Units>> visitBinary(BinaryTree tree, ExtendedTypeMirror type) {

                if (TreeUtils.isStringConcatenation(tree)
                        || (tree instanceof CompoundAssignmentTree
                        && TreeUtils.isStringCompoundConcatenation((CompoundAssignmentTree)tree))) {

                    QualParams<Units> lUnits = getEffectiveQualifier(getQualifiedType(tree.getLeftOperand()));
                    QualParams<Units> rUnits = getEffectiveQualifier(getQualifiedType(tree.getRightOperand()));
                    QualifiedTypeMirror<QualParams<Units>> result =
                            handleBinaryOperation(tree, lUnits, rUnits, type);
                    if (result != null) {
                        return result;
                    }
                }
                return super.visitBinary(tree, type);
            }

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
            private QualifiedTypeMirror<QualParams<Units>> handleBinaryOperation(Tree tree, QualParams<Units> lUnitsParam,
                    QualParams<Units> rUnitsParam, ExtendedTypeMirror type) {

                if (TreeUtils.isStringConcatenation(tree)
                        || (tree instanceof CompoundAssignmentTree
                            && TreeUtils.isStringCompoundConcatenation((CompoundAssignmentTree)tree))) {

                    PolyQual<Units> resultQual = null;

                    PolyQual<Units> rPrimary = rUnitsParam.getPrimary();
                    PolyQual<Units> lPrimary = lUnitsParam.getPrimary();

                    Units rUnits = getQualifierHierarchy().getBottom() == rUnitsParam ?
                            new Units.UnitsVal(0) : rPrimary.getMaximum();
                    Units lUnits = getQualifierHierarchy().getBottom() == lUnitsParam ?
                            new Units.UnitsVal(0) : lPrimary.getMaximum();

                    PolyQual<Units> polyResult = checkPoly(lPrimary, rPrimary, lUnits, rUnits);
                    if (polyResult != null) {
                        resultQual = polyResult;

                    } else if (lUnits.isUnitsVal() && rUnits.isUnitsVal()) {
                        // Units(a) + Units(b) = Units(a + b)
                        int resultCount = ((Units.UnitsVal) lUnits).getCount() + ((Units.UnitsVal) rUnits).getCount();
                        resultQual = new GroundQual<Units>(new Units.UnitsVal(resultCount));

                    } else if (lUnits.isPartialUnits() && rUnits.isPartialUnits()) {
                        // Partial + Partial == Units or Partial
                        String concat = ((Units.PartialUnits) lUnits).getPartialValue() + ((Units.PartialUnits) rUnits).getPartialValue();
                        if (isUnits(concat)) {
                            int groupCount = getGroupCount(concat);
                            resultQual = new GroundQual<Units>(new Units.UnitsVal(groupCount));
                        } else {
                            resultQual = new GroundQual<Units>(new Units.PartialUnits(concat));
                        }

                    } else if (lUnits.isUnitsVal() && rUnits.isPartialUnits()) {
                        // Units + Partial == Partial
                        String concat = "e" + ((Units.PartialUnits) rUnits).getPartialValue();
                        resultQual = new GroundQual<Units>(new Units.PartialUnits(concat));

                    } else if (rUnits.isUnitsVal() && lUnits.isPartialUnits()) {
                        // Partial + Units == Partial
                        String concat = ((Units.PartialUnits) lUnits).getPartialValue() + "e";
                        resultQual = new GroundQual<Units>(new Units.PartialUnits(concat));
                    } else if (rUnits == Units.TOP || lUnits == Units.TOP) {
                        resultQual = new GroundQual<>(Units.TOP);
                    } else if (rUnits == Units.BOTTOM && lUnits == Units.BOTTOM) {
                        resultQual = new GroundQual<>(Units.BOTTOM);
                    }

                    if (resultQual != null) {
                        return new QualifiedDeclaredType<>(type, new QualParams<>(resultQual),
                                new ArrayList<QualifiedTypeMirror<QualParams<Units>>>());
                    }
                }

                return null;
            }

        }; // End of return new QualifierParameterTreeAnnotator<Units>(this)
    }

    /**
     * Check to see if the result of the operation is polymorphic.
     *
     * @return the polymorphic PolyQual if the result should be polymorphic, otherwise return null.
     */
    private PolyQual<Units> checkPoly(PolyQual<Units> lPrimary, PolyQual<Units> rPrimary, Units lUnits, Units rUnits) {
        if (isPolyUnits(lPrimary) && isPolyUnits(rPrimary)) {
            return lPrimary;
        } else if (isPolyUnits(lPrimary) && rUnits.isUnitsVal()) {
            return lPrimary;
        } else if (isPolyUnits(rPrimary) && lUnits.isUnitsVal()) {
            return rPrimary;
        } else {
            return null;
        }
    }

    // checks to see if the qualifier is a polymorphic qualifier
    private boolean isPolyUnits(PolyQual<Units> possiblePoly) {
        // it is a poly qualifier if it is a qual variable and
        
        return (possiblePoly instanceof QualVar)
        // it's name is the same as "_poly", or whichever is hard coded into the simple qual param annotation converter
                && ((QualVar<?>) possiblePoly).getName().equals(SimpleQualifierParameterAnnotationConverter.POLY_NAME);
    }

    /**
     * Returns the number of groups in the given Units String.
     */
    public static int getGroupCount(/*@org.checkerframework.checker.Units.qual.Units*/ String Units) {

        return java.util.regex.Pattern.compile(Units).matcher("").groupCount();
    }

    /** This method is a copy of UnitsUtil.isValidUnits.
     * We cannot directly use UnitsUtil, because it uses type annotations
     * which cannot be used in IDEs (yet).
     */
    /*@SuppressWarnings("purity")*/ // the checker cannot prove that the method is pure, but it is
    /*@org.checkerframework.dataflow.qual.Pure*/
    private static boolean isUnits(String s) {
        try {
            Pattern.compile(s);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
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
