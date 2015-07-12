package org.checkerframework.checker.experimental.units_qual_poly;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;

import org.checkerframework.checker.experimental.Units_qual.Units;
import org.checkerframework.framework.source.Result;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.qualframework.base.CheckerAdapter;
import org.checkerframework.qualframework.base.QualifiedTypeMirror;
import org.checkerframework.qualframework.base.TypecheckVisitorAdapter;
import org.checkerframework.qualframework.poly.QualParams;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * The {@link org.checkerframework.common.basetype.BaseTypeVisitor} for the Units-Qual-Param type system.
 */
public class UnitsTypecheckVisitor extends TypecheckVisitorAdapter<QualParams<Units>> {

    private final ExecutableElement matchResultEnd;
    private final ExecutableElement matchResultGroup;
    private final ExecutableElement matchResultStart;
    private final ExecutableElement patternCompile;
    private final VariableElement patternLiteral;

    public UnitsTypecheckVisitor(CheckerAdapter<QualParams<Units>> checker) {
        super(checker);
        ProcessingEnvironment env = checker.getProcessingEnvironment();
        this.matchResultEnd = TreeUtils.getMethod("java.util.Units.MatchResult", "end", 1, env);
        this.matchResultGroup = TreeUtils.getMethod("java.util.Units.MatchResult", "group", 1, env);
        this.matchResultStart = TreeUtils.getMethod("java.util.Units.MatchResult", "start", 1, env);
        this.patternCompile = TreeUtils.getMethod("java.util.Units.Pattern", "compile", 2, env);
        this.patternLiteral = TreeUtils.getField("java.util.Units.Pattern", "LITERAL", env);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        /**
         * Case 1: Don't require a Units.UnitsVal qualifier on the String argument to
         * Pattern.compile if the Pattern.LITERAL flag is passed.
         */
        ProcessingEnvironment env = checker.getProcessingEnvironment();
        if (TreeUtils.isMethodInvocation(node, patternCompile, env)) {
            ExpressionTree flagParam = node.getArguments().get(1);
            if (flagParam.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree memSelect = (MemberSelectTree) flagParam;
                if (TreeUtils.isSpecificFieldAccess(memSelect, patternLiteral)) {
                    // This is a call to Pattern.compile with the Pattern.LITERAL
                    // flag so the first parameter doesn't need to be a
                    // @Units String. Don't call the super method to skip checking
                    // if the first parameter is a @Units String, but make sure to
                    // still recurse on all of the different parts of the method call.
                    Void r = scan(node.getTypeArguments(), p);
                    r = reduce(scan(node.getMethodSelect(), p), r);
                    r = reduce(scan(node.getArguments(), p), r);
                    return r;
                }
            }
        } else if (TreeUtils.isMethodInvocation(node, matchResultEnd, env)
                || TreeUtils.isMethodInvocation(node, matchResultGroup, env)
                || TreeUtils.isMethodInvocation(node, matchResultStart, env)) {
            /**
             * Case 3: Checks calls to {@code MatchResult.start}, {@code MatchResult.end}
             * and {@code MatchResult.group} to ensure that a valid group number is passed.
             */
            ExpressionTree group = node.getArguments().get(0);
            if (group.getKind() == Tree.Kind.INT_LITERAL) {
                LiteralTree literal = (LiteralTree) group;
                int paramGroups = (Integer) literal.getValue();
                ExpressionTree receiver = TreeUtils.getReceiverTree(node);
                int annoGroups = 0;
                QualifiedTypeMirror<QualParams<Units>> receiverType = context.getTypeFactory().getQualifiedType(receiver);
                Units Units = receiverType.getQualifier().getPrimary().getMaximum();
                if (paramGroups > 0 &&
                        (!Units.isUnitsVal() || ((Units.UnitsVal) Units).getCount() < paramGroups)) {
                    checker.report(Result.failure("group.count.invalid", paramGroups, annoGroups, receiver), group);
                }
            } else {
                checker.report(Result.warning("group.count.unknown"), group);
            }
        }
        return super.visitMethodInvocation(node, p);
    }
}
