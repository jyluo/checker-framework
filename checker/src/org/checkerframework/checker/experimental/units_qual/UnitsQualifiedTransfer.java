package org.checkerframework.checker.experimental.units_qual;

import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;

import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.ClassNameNode;
import org.checkerframework.dataflow.cfg.node.MethodAccessNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.qualframework.base.dataflow.QualAnalysis;
import org.checkerframework.qualframework.base.dataflow.QualStore;
import org.checkerframework.qualframework.base.dataflow.QualTransfer;
import org.checkerframework.qualframework.base.dataflow.QualValue;

public class UnitsQualifiedTransfer extends QualTransfer<Units> {

    public UnitsQualifiedTransfer(QualAnalysis<Units> analysis) {
        super(analysis);
    }

    @Override
    public TransferResult<QualValue<Units>, QualStore<Units>> visitMethodInvocation(
            MethodInvocationNode n, TransferInput<QualValue<Units>, QualStore<Units>> in) {

        TransferResult<QualValue<Units>, QualStore<Units>> result = super.visitMethodInvocation(n, in);

      //  analysis.getEnv().getMessager().printMessage(Kind.NOTE, " Transfer method invoc ");
        
        // refine result for some helper methods
        MethodAccessNode target = n.getTarget();
        ExecutableElement method = target.getMethod();
        Node receiver = target.getReceiver();
        if (!(receiver instanceof ClassNameNode)) {
            return result;
        }
        ClassNameNode cn = (ClassNameNode) receiver;
        String receiverName = cn.getElement().toString();

        
        
        return result;
    }

    /**
     * Returns true if the given receiver is a class named "RegexUtil".
     */
    private boolean isRegexUtil(String receiver) {
        return receiver.equals("RegexUtil") || receiver.endsWith(".RegexUtil");
    }
}
