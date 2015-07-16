package org.checkerframework.checker.experimental.units_qual_poly;

import org.checkerframework.checker.experimental.units_qual_poly.Units;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.ClassNameNode;
import org.checkerframework.dataflow.cfg.node.IntegerLiteralNode;
import org.checkerframework.dataflow.cfg.node.MethodAccessNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.qual.EnsuresQualifierIf;
import org.checkerframework.framework.qual.EnsuresQualifiersIf;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.FlowExpressionParseUtil;
import org.checkerframework.framework.util.FlowExpressionParseUtil.FlowExpressionContext;
import org.checkerframework.framework.util.FlowExpressionParseUtil.FlowExpressionParseException;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.qualframework.base.QualifiedTypeMirror;
import org.checkerframework.qualframework.base.dataflow.QualAnalysis;
import org.checkerframework.qualframework.base.dataflow.QualStore;
import org.checkerframework.qualframework.base.dataflow.QualTransfer;
import org.checkerframework.qualframework.base.dataflow.QualValue;
import org.checkerframework.qualframework.poly.PolyQual.GroundQual;
import org.checkerframework.qualframework.poly.QualParams;

import javax.lang.model.element.ExecutableElement;

/**
 * A reimplementation of {@link UnitsTransfer} using {@link QualifiedTypeMirror}s
 * instead of {@link AnnotatedTypeMirror}s.
 */
public class UnitsQualifiedTransfer extends QualTransfer<QualParams<Units>> {

    private static final String IS_Units_METHOD_NAME = "isUnits";
    private static final String AS_Units_METHOD_NAME = "asUnits";

    public UnitsQualifiedTransfer(QualAnalysis<QualParams<Units>> analysis) {
        super(analysis);
    }

    // polymorphic Unit can only be applied to methods
    // ie @PolyUnit addThree(@PolyUnit int x);
    // which would accept any unit and return that exact unit times 3

    // detect the unit that's in the store, and return the unit (this would be high level purpose)

    // This whole file is used to assist the RegexUtil class to construct Regex Qualifiers via its two helper methods isRegex and asRegex rather than annotations
    // might be smart idea for Units qual checker. Building units via a asUnits method.

    @Override
    public TransferResult<QualValue<QualParams<Units>>, QualStore<QualParams<Units>>> visitMethodInvocation(
            MethodInvocationNode n, 
            TransferInput<QualValue<QualParams<Units>>, QualStore<QualParams<Units>>> in
            ) {

        TransferResult<QualValue<QualParams<Units>>, QualStore<QualParams<Units>>> result = super.visitMethodInvocation(n, in);

        /*

        // refine result for some helper methods
        MethodAccessNode target = n.getTarget();                // get node?
        ExecutableElement method = target.getMethod();          // get method of node?
        Node receiver = target.getReceiver();                   // get receiver of node

        // make sure the receiver is some kind of node that has a class name associated with it, as in static method invocations : Math.power
        if (!(receiver instanceof ClassNameNode)) {
            return result;
        }
        ClassNameNode cn = (ClassNameNode) receiver;
        String receiverName = cn.getElement().toString();

        // true if the receiving class's name is UnitsUtil
        if (isUnitsUtil(receiverName)) {

            // check to see if receiver method signature matches
            // RegexUntil has only 2 transfer methods:
            // Regex.isRegex(String regex string, int groups) returns true if the string is a regex string with >= group count than the int passed in
            // Regex.asRegex(String string) returns a @Regex(groups) string of the input string, if the string compiles


            if (ElementUtils.matchesElement(method,
                    IS_Units_METHOD_NAME, String.class, int.class)) {
                // UnitsUtil.isUnits(s, groups) method
                // (No special case is needed for isUnits(String) because of
                // the annotation on that method's definition.)

//                @EnsuresQualifiersIf({
//                  @EnsuresQualifierIf(result=true, expression="#1", qualifier=Regex.class)})
//                  public static boolean isRegex(String s)
//
//                 public static boolean isRegex(String s, int groups)
//
//                 @EnsuresQualifiersIf({
//                  @EnsuresQualifierIf(result=true, expression="#1", qualifier=Regex.class)})
//                  public static boolean isRegex(final char c)


                QualStore<QualParams<Units>> thenStore = result.getRegularStore();      // starting store
                QualStore<QualParams<Units>> elseStore = thenStore.copy();              // result store?, initally a copy of starting

                // create and return a conditional transfer result
                ConditionalTransferResult<QualValue<QualParams<Units>>, QualStore<QualParams<Units>>> 
                newResult = new ConditionalTransferResult<>(result.getResultValue(), thenStore, elseStore);

                // get the context of the node
                FlowExpressionContext context = FlowExpressionParseUtil
                        .buildFlowExprContextForUse(n, analysis.getContext());
                try {
                    Receiver firstParam = FlowExpressionParseUtil.parse(
                            "#1", context, analysis.getContext().getTypeFactory().getPath(n.getTree()));
                    // add annotation with correct group count (if possible,
                    // Units annotation without count otherwise)
                    Node count = n.getArgument(1);      // get the node of the second parameter
                    if (count instanceof IntegerLiteralNode) {          // if that node is an Integer Literal
                        IntegerLiteralNode iln = (IntegerLiteralNode) count;
                        Integer groupCount = iln.getValue();    // get the value of that int literal
                        Units Units = new UnitsVal(groupCount);         // create the Regex Qualifier with that many groups
                        thenStore.insertValue(firstParam, new QualParams<>(new GroundQual<>(Units)));    // insert this Qualifier as a Ground Qualifier in the then store
                    } else {
                        thenStore.insertValue(firstParam, new QualParams<>(new GroundQual<Units>(new UnitsVal(0))));    // if somehow the second parameter is not an integer, create and return a Regex Qualifier with count = 0
                    }
                } catch (FlowExpressionParseException e) {
                    assert false;
                }
                return newResult;       // returns the transfer result (stores reference to thenStore?)

            } else if (ElementUtils.matchesElement(method,
                    AS_Units_METHOD_NAME, String.class, int.class)) {

                // Regex.asRegex(String string) returns a @Regex(groups) string of the input string, if the string compiles
                // public static @Regex String asRegex(String s, int groups) returns a @Regex(groups) string of the input string if the string compiles and has at least the number of groups passed in as parameter


                // UnitsUtil.asUnits(s, groups) method
                // (No special case is needed for asUnits(String) because of
                // the annotation on that method's definition.)

                // add annotation with correct group count (if possible,
                // Units annotation without count otherwise)
                QualParams<Units> Units;
                Node count = n.getArgument(1);          // get the second parameter
                if (count instanceof IntegerLiteralNode) {      // check to see that it is an integer
                    IntegerLiteralNode iln = (IntegerLiteralNode) count;
                    Integer groupCount = iln.getValue();
                    Units = new QualParams<>(new GroundQual<Units>(new UnitsVal(groupCount)));          // if so, construct a Qualifier with that number of groups specified
                } else {
                    Units = new QualParams<>(new GroundQual<Units>(new UnitsVal(0)));   // otherwise construct a Qualifier with 0 groups
                }

                // construct a Qualifier Result with the Qualifier added in
                QualValue<QualParams<Units>> newResultValue = analysis
                        .createSingleAnnotationValue(Units,
                                result.getResultValue().getType().getUnderlyingType().getOriginalType());

                // construct and return the transfer result with the new qualifier result added in
                return new RegularTransferResult<>(newResultValue,
                        result.getRegularStore());
            }
        }

        */
        return result;
    }

    /**
     * Returns true if the given receiver is a class named "UnitsUtil".
     */
    private boolean isUnitsUtil(String receiver) {
        return receiver.equals("UnitsUtil") || receiver.endsWith(".UnitsUtil");
    }
}
